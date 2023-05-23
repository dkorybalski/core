package pl.edu.amu.wmi.service.impl;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.mapper.SupervisorMapper;
import pl.edu.amu.wmi.model.NewSupervisorDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.DataFeedService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DataFeedSupervisorServiceImpl implements DataFeedService {

    private final SupervisorMapper supervisorMapper;

    private final SupervisorDAO supervisorDAO;

    public DataFeedSupervisorServiceImpl(SupervisorMapper supervisorMapper, SupervisorDAO supervisorDAO) {
        this.supervisorMapper = supervisorMapper;
        this.supervisorDAO = supervisorDAO;
    }


    @Override
    public DataFeedType getType() {
        return DataFeedType.NEW_SUPERVISOR;
    }

    @Override
    public void saveRecords(MultipartFile data, String studyYear) {
        List<NewSupervisorDTO> newSupervisors = parseDataFromFileToObjects(data);
        saveNewSupervisors(newSupervisors, studyYear);
    }

    private List<NewSupervisorDTO> parseDataFromFileToObjects(MultipartFile data) {
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
                // TODO: 5/11/2023 add custom exception
                e.printStackTrace();
            }
        }
        return newSupervisors;
    }

    public List<NewSupervisorDTO> saveNewSupervisors(List<NewSupervisorDTO> newSupervisors, String studyYear) {
        List<Supervisor> entities = supervisorMapper.mapToEntities(newSupervisors, studyYear);
        List<Supervisor> supervisors = supervisorDAO.saveAll(entities);
        return supervisorMapper.mapToDTOs(supervisors);
    }
}
