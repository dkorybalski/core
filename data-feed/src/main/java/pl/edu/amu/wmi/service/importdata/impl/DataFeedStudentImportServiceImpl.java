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
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.StudyYearDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.entity.StudyYear;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.mapper.StudentMapper;
import pl.edu.amu.wmi.model.NewStudentDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.importdata.DataFeedImportService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class DataFeedStudentImportServiceImpl implements DataFeedImportService {

    private static final String INDEX_REGEX_PATTERN = "^s\\d{6}";

    private final StudentMapper studentMapper;

    private final StudentDAO studentDAO;

    private final StudyYearDAO studyYearDAO;

    private final RoleDAO roleDAO;

    private final UserDataDAO userDataDAO;

    public DataFeedStudentImportServiceImpl(StudentMapper studentMapper,
                                            StudentDAO studentDAO,
                                            StudyYearDAO studyYearDAO,
                                            RoleDAO roleDAO,
                                            UserDataDAO userDataDAO) {
        this.studentMapper = studentMapper;
        this.studentDAO = studentDAO;
        this.studyYearDAO = studyYearDAO;
        this.roleDAO = roleDAO;
        this.userDataDAO = userDataDAO;
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.NEW_STUDENT;
    }

    @Transactional
    @Override
    public void saveRecords(MultipartFile data, String studyYear) throws CsvException {
        List<NewStudentDTO> newStudents = parseDataFromFileToObjects(data);
        validateData(newStudents, studyYear);
        saveNewStudents(newStudents, studyYear);
    }

    private void validateData(List<NewStudentDTO> newStudents, String studyYear) {
        List<String> studentIndexNumbers = newStudents.stream()
                .map(NewStudentDTO::getIndexNumber)
                .toList();
        List<Student> existingStudentsForStudyYear = studentDAO.findByStudyYearAndUserData_IndexNumberIn(studyYear, studentIndexNumbers);
        if (!existingStudentsForStudyYear.isEmpty()) {
            log.error("Duplicated data - {} students assigned to studyYear {} already exist in the database.", existingStudentsForStudyYear.size(), studyYear);
            throw new DuplicateKeyException("Duplicated student data");
        }
    }

    private List<NewStudentDTO> parseDataFromFileToObjects(MultipartFile data) throws CsvException {
        // TODO: 5/12/2023 add validation if data were saved
        List<NewStudentDTO> newStudents = new ArrayList<>();
        if (data.isEmpty()) {
            log.info("File with students data is empty");
        } else {
            try (Reader reader = new BufferedReader(new InputStreamReader(data.getInputStream()))){

                CsvToBean<NewStudentDTO> csvToBean = new CsvToBeanBuilder<NewStudentDTO>(reader)
                        .withType(NewStudentDTO.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withIgnoreQuotations(true)
                        .withSeparator(';')
                        .build();

                newStudents = csvToBean.parse();
            } catch (IOException e) {
                log.error("Error during parsing csv file with students data", e);
                throw new CsvException();
            }
        }
        return newStudents;
    }

    public List<NewStudentDTO> saveNewStudents(List<NewStudentDTO> newStudents, String studyYear) {
        List<Student> entities = studentMapper.mapToEntities(newStudents);
        StudyYear studyYearEntity = studyYearDAO.findByStudyYear(studyYear);
        String indexNumberWithPrefix;
        for (Student student : entities) {
            if (!validateIndexNumber(student.getIndexNumber())) {
                indexNumberWithPrefix = addPrefixToIndex(student.getIndexNumber());
                student.getUserData().setIndexNumber(indexNumberWithPrefix);
            } else {
                indexNumberWithPrefix = student.getIndexNumber();
            }
            student.setStudyYear(studyYearEntity.getStudyYear());

            if (Boolean.TRUE.equals(userDataDAO.existsByIndexNumber(indexNumberWithPrefix))) {
                UserData userData = userDataDAO.findByIndexNumber(indexNumberWithPrefix).orElseThrow(() ->
                        new BusinessException("Unexpected exception during fetching user data"));
                student.setUserData(userData);
            }

            // TODO: 11/22/2023 check if roles works correctly when student has many study years
            student.getUserData().setRoles(Set.of(roleDAO.findByName(UserRole.STUDENT)));
        }
        List<Student> students = studentDAO.saveAll(entities);
        return studentMapper.mapToDTOs(students);
    }

    private String addPrefixToIndex(String indexNumber) {
        return "s" + indexNumber;
    }

    private boolean validateIndexNumber(String indexNumber) {
        return indexNumber.matches(INDEX_REGEX_PATTERN);
    }
}
