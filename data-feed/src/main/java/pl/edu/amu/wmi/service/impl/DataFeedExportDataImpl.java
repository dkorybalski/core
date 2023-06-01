package pl.edu.amu.wmi.service.impl;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.StudyYearDAO;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.entity.StudyYear;
import pl.edu.amu.wmi.service.DataFeedExportService;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Service
@Slf4j
public class DataFeedExportDataImpl implements DataFeedExportService {

    private final StudentDAO studentDAO;

    private final StudyYearDAO studyYearDAO;

    public DataFeedExportDataImpl(StudentDAO studentDAO, StudyYearDAO studyYearDAO) {
        this.studentDAO = studentDAO;
        this.studyYearDAO = studyYearDAO;
    }

    @Override
    public void exportData(Writer writer, String studyYearName) throws CsvException {
        try (CSVWriter csvWriter = new CSVWriter(writer, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);) {

            csvWriter.writeNext(createHeaders());

            List<Student> students = studentDAO.findAll();
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
                student.getUserData().getIndexNumber(),
                student.getUserData().getFirstName(),
                student.getUserData().getLastName(),
                student.getUserData().getEmail(),
                // TODO: 5/30/2023 logic to set proper cyd code
                studyYear.getFirstSemesterCode(),
                studyYear.getSubjectCode(),
                studyYear.getSubjectType(),
                // TODO: 5/30/2023 implement the group number
                "",
                ""
        };
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
        private final static String PESEL = "PESEL";
        private final static String INDEKS = "INDEKS";
        private final static String IMIE = "IMIE";
        private final static String NAZWISKO = "NAZWISKO";
        private final static String EMAIL = "EMAIL";
        private final static String CDYD_KOD = "CDYD_KOD";
        private final static String PRZ_KOD = "PRZ_KOD";
        private final static String TZAJ_KOD = "TZAJ_KOD";
        private final static String GR_NR = "GR_NR";
        private final static String PRG_KOD = "PRG_KOD";
    }
}
