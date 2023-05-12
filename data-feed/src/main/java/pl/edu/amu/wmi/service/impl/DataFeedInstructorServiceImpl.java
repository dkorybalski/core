package pl.edu.amu.wmi.service.impl;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.dao.InstructorDAO;
import pl.edu.amu.wmi.entity.Instructor;
import pl.edu.amu.wmi.mapper.InstructorMapper;
import pl.edu.amu.wmi.model.NewInstructorDTO;
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
public class DataFeedInstructorServiceImpl implements DataFeedService {

    private final InstructorMapper instructorMapper;

    private final InstructorDAO instructorDAO;

    public DataFeedInstructorServiceImpl(InstructorMapper instructorMapper, InstructorDAO instructorDAO) {
        this.instructorMapper = instructorMapper;
        this.instructorDAO = instructorDAO;
    }


    @Override
    public DataFeedType getType() {
        return DataFeedType.NEW_INSTRUCTOR;
    }

    @Override
    public void saveRecords(MultipartFile data, String studyYear) {
        List<NewInstructorDTO> newInstructors = parseDataFromFileToObjects(data);
        saveNewInstructors(newInstructors, studyYear);
    }

    private List<NewInstructorDTO> parseDataFromFileToObjects(MultipartFile data) {
        // TODO: 5/12/2023 add validation if data were saved
        List<NewInstructorDTO> newInstructors = new ArrayList<>();
        if (data.isEmpty()) {
            log.info("File with instructors data is empty");
        } else {
            try (Reader reader = new BufferedReader(new InputStreamReader(data.getInputStream()))){

                CsvToBean<NewInstructorDTO> csvToBean = new CsvToBeanBuilder<NewInstructorDTO>(reader)
                        .withType(NewInstructorDTO.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withIgnoreQuotations(true)
                        .withSeparator(';')
                        .build();

                newInstructors = csvToBean.parse();
            } catch (IOException e) {
                // TODO: 5/11/2023 add custom exception
                e.printStackTrace();
            }
        }
        return newInstructors;
    }

    public List<NewInstructorDTO> saveNewInstructors(List<NewInstructorDTO> newInstructors, String studyYear) {
        List<Instructor> entities = instructorMapper.mapToEntities(newInstructors, studyYear);
        List<Instructor> instructors = instructorDAO.saveAll(entities);
        return instructorMapper.mapToDTOs(instructors);
    }
}
