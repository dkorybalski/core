package pl.edu.amu.wmi.service.exportdata.impl;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.StudyYearDAO;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.entity.StudyYear;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.exportdata.DataFeedExportService;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Component
@Slf4j
public class DataFeedStudentGroupsExportServiceImpl implements DataFeedExportService {

    private final StudentDAO studentDAO;

    private final StudyYearDAO studyYearDAO;

    public DataFeedStudentGroupsExportServiceImpl(StudentDAO studentDAO, StudyYearDAO studyYearDAO) {
        this.studentDAO = studentDAO;
        this.studyYearDAO = studyYearDAO;
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.STUDENT_GROUPS;
    }

    @Override
    public void exportData(Writer writer, String studyYearName) throws Exception {
        try (CSVWriter csvWriter = new CSVWriter(writer, ';', ICSVWriter.NO_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END);) {

            csvWriter.writeNext(createHeaders());

            List<Student> students = studentDAO.findAllByStudyYear(studyYearName);
            StudyYear studyYear = studyYearDAO.findByStudyYear(studyYearName);

            for (Student student : students) {
                csvWriter.writeNext(createStudentData(student, studyYear));
            }
        } catch (IOException e) {
            log.error("Error during parsing csv file with students data", e);
            throw new CsvException();
        }
    }

    private String[] createStudentData(Student student, StudyYear studyYear) {
        return new String[]{student.getPesel(),
                student.getIndexNumber(),
                student.getUserData().getFirstName(),
                student.getUserData().getLastName(),
                student.getUserData().getEmail(),
                // TODO: 5/30/2023 logic to set proper cyd code
                studyYear.getFirstSemesterCode(),
                studyYear.getSubjectCode(),
                studyYear.getSubjectType(),
                // TODO: 5/30/2023 implement the group number
                getGroupNumber(student),
                ""
        };
    }

    private String getGroupNumber(Student student) {
        // TODO: 6/23/2023 use better method from performance point of view
        if (student.getConfirmedProject() != null) {
            return student.getConfirmedProject().getSupervisor().getGroupNumber().toString();
        }
        return "";
    }

    private String[] createHeaders() {
        return new String[]{CSVHeaders.PESEL,
                CSVHeaders.INDEKS,
                CSVHeaders.IMIE,
                CSVHeaders.NAZWISKO,
                CSVHeaders.EMAIL,
                CSVHeaders.CDYD_KOD,
                CSVHeaders.PRZ_KOD,
                CSVHeaders.TZAJ_KOD,
                CSVHeaders.GR_NR,
                CSVHeaders.PRG_KOD};
    }

    private static class CSVHeaders {
        private static final String PESEL = "PESEL";
        private static final String INDEKS = "INDEKS";
        private static final String IMIE = "IMIE";
        private static final String NAZWISKO = "NAZWISKO";
        private static final String EMAIL = "EMAIL";
        private static final String CDYD_KOD = "CDYD_KOD";
        private static final String PRZ_KOD = "PRZ_KOD";
        private static final String TZAJ_KOD = "TZAJ_KOD";
        private static final String GR_NR = "GR_NR";
        private static final String PRG_KOD = "PRG_KOD";
    }
}
