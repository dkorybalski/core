package pl.edu.amu.wmi.service.impl;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.mapper.StudentMapper;
import pl.edu.amu.wmi.model.NewStudentDTO;
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
public class DataFeedStudentServiceImpl implements DataFeedService {

    private final StudentMapper studentMapper;

    private final StudentDAO studentDAO;

    public DataFeedStudentServiceImpl(StudentMapper studentMapper, StudentDAO studentDAO) {
        this.studentMapper = studentMapper;
        this.studentDAO = studentDAO;
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.NEW_STUDENT;
    }

    @Override
    public void saveRecords(MultipartFile data, String studyYear) {
        List<NewStudentDTO> newStudents = parseDataFromFileToObjects(data);
        saveNewStudents(newStudents, studyYear);
    }

    private List<NewStudentDTO> parseDataFromFileToObjects(MultipartFile data) {
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
                // TODO: 5/11/2023 add custom exception
                e.printStackTrace();
            }
        }
        return newStudents;
    }

    public List<NewStudentDTO> saveNewStudents(List<NewStudentDTO> newStudents, String studyYear) {
        List<Student> entities = studentMapper.mapToEntities(newStudents, studyYear);
        List<Student> students = studentDAO.saveAll(entities);
        return studentMapper.mapToDTOs(students);
    }
}
