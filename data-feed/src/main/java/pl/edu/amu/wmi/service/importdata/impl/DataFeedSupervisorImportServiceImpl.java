package pl.edu.amu.wmi.service.importdata.impl;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.dao.RoleDAO;
import pl.edu.amu.wmi.dao.StudyYearDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.StudyYear;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.mapper.SupervisorMapper;
import pl.edu.amu.wmi.model.NewSupervisorDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.importdata.DataFeedImportService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DataFeedSupervisorImportServiceImpl implements DataFeedImportService {

    private final SupervisorMapper supervisorMapper;

    private final SupervisorDAO supervisorDAO;

    private final StudyYearDAO studyYearDAO;

    private final RoleDAO roleDAO;

    private final UserDataDAO userDataDAO;

    public DataFeedSupervisorImportServiceImpl(SupervisorMapper supervisorMapper,
                                               SupervisorDAO supervisorDAO,
                                               StudyYearDAO studyYearDAO,
                                               RoleDAO roleDAO,
                                               UserDataDAO userDataDAO) {
        this.supervisorMapper = supervisorMapper;
        this.supervisorDAO = supervisorDAO;
        this.studyYearDAO = studyYearDAO;
        this.roleDAO = roleDAO;
        this.userDataDAO = userDataDAO;
    }


    @Override
    public DataFeedType getType() {
        return DataFeedType.NEW_SUPERVISOR;
    }

    @Transactional
    @Override
    public void saveRecords(MultipartFile data, String studyYear) throws CsvException {
        List<NewSupervisorDTO> newSupervisors = parseDataFromFileToObjects(data);
        validateRecords(newSupervisors, studyYear);
        saveNewSupervisors(newSupervisors, studyYear);
    }

    private void validateRecords(List<NewSupervisorDTO> newSupervisors, String studyYear) {
        List<String> supervisorIndexNumbers = newSupervisors.stream()
                .map(NewSupervisorDTO::getIndexNumber)
                .toList();
        List<Supervisor> existingSupervisorsForStudyYear = supervisorDAO.findAllByStudyYearAndUserData_IndexNumberIn(studyYear, supervisorIndexNumbers);
        if (!existingSupervisorsForStudyYear.isEmpty()) {
            log.error("Duplicated data - {} supervisors assigned to studyYear {} already exist in the database.", existingSupervisorsForStudyYear.size(), studyYear);
            throw new DuplicateKeyException("Duplicated supervisor data");
        }
    }

    private List<NewSupervisorDTO> parseDataFromFileToObjects(MultipartFile data) throws CsvException {
        // TODO: 5/12/2023 add validation if data were saved
        List<NewSupervisorDTO> newSupervisors = new ArrayList<>();
        if (data.isEmpty()) {
            log.info("File with supervisors data is empty");
        } else {
            try (Reader reader = new BufferedReader(new InputStreamReader(data.getInputStream()))){

                CsvToBean<NewSupervisorDTO> csvToBean = new CsvToBeanBuilder<NewSupervisorDTO>(reader)
                        .withType(NewSupervisorDTO.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withIgnoreQuotations(true)
                        .withSeparator(';')
                        .build();

                newSupervisors = csvToBean.parse();
            } catch (IOException e) {
                log.error("Error during parsing csv file with students data", e);
                throw new CsvException();
            }
        }
        return newSupervisors;
    }

    public List<NewSupervisorDTO> saveNewSupervisors(List<NewSupervisorDTO> newSupervisors, String studyYear) {
        List<Supervisor> entities = supervisorMapper.mapToEntities(newSupervisors);
        StudyYear studyYearEntity = studyYearDAO.findByStudyYear(studyYear);
        for (Supervisor supervisor : entities) {
            String indexNumberWithPrefix = supervisor.getIndexNumber();
            if (Boolean.TRUE.equals(userDataDAO.existsByIndexNumber(indexNumberWithPrefix))) {
                UserData userData = userDataDAO.findByIndexNumber(indexNumberWithPrefix).orElseThrow(() ->
                        new BusinessException("Unexpected exception during fetching user data"));
                supervisor.setUserData(userData);
            }
            supervisor.setStudyYear(studyYearEntity.getStudyYear());
            supervisor.getUserData().getRoles().add(roleDAO.findByName(UserRole.SUPERVISOR));
        }
        List<Supervisor> supervisors = supervisorDAO.saveAll(entities);
        return supervisorMapper.mapToDTOs(supervisors);
    }
}
