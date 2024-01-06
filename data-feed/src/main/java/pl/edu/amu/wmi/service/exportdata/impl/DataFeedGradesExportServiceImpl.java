package pl.edu.amu.wmi.service.exportdata.impl;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.StudyYearDAO;
import pl.edu.amu.wmi.entity.EvaluationCard;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.exportdata.DataFeedExportService;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class DataFeedGradesExportServiceImpl implements DataFeedExportService {

    private final StudentDAO studentDAO;

    private final StudyYearDAO studyYearDAO;

    public DataFeedGradesExportServiceImpl(StudentDAO studentDAO, StudyYearDAO studyYearDAO) {
        this.studentDAO = studentDAO;
        this.studyYearDAO = studyYearDAO;
    }

    @Override
    public void exportData(Writer writer, String studyYear) throws Exception {
        try (CSVWriter csvWriter = new CSVWriter(writer, ';', ICSVWriter.NO_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END);) {
            csvWriter.writeNext(createHeaders());
            List<Student> students = studentDAO.findAllByStudyYear(studyYear);

            for (Student student : students) {
                if (Objects.nonNull(student.getConfirmedProject())) {
                    csvWriter.writeNext(createGradesData(student));
                }
            }
        } catch (IOException e) {
            log.error("Error during parsing csv file with grades data", e);
            throw new CsvException();
        }
    }

    private String[] createGradesData(Student student) {
        List<EvaluationCard> evaluationCards = student.getConfirmedProject().getEvaluationCards();

        return new String[]{
                student.getIndexNumber(),
                extractGradeForSemesterAndTerm(evaluationCards, Semester.FIRST, EvaluationPhase.DEFENSE_PHASE, EvaluationStatus.PUBLISHED),
                extractGradeForSemesterAndTerm(evaluationCards, Semester.FIRST, EvaluationPhase.RETAKE_PHASE, EvaluationStatus.RETAKE),
                extractGradeForSemesterAndTerm(evaluationCards, Semester.SECOND, EvaluationPhase.DEFENSE_PHASE, EvaluationStatus.PUBLISHED),
                extractGradeForSemesterAndTerm(evaluationCards, Semester.SECOND, EvaluationPhase.RETAKE_PHASE, EvaluationStatus.RETAKE)
        };
    }

    private String extractGradeForSemesterAndTerm(List<EvaluationCard> evaluationCards, Semester semester, EvaluationPhase phase, EvaluationStatus status) {
        EvaluationCard evaluationCard = evaluationCards.stream()
                .filter(card -> Objects.equals(semester, card.getSemester())
                        && Objects.equals(phase, card.getEvaluationPhase())
                        && Objects.equals(status, card.getEvaluationStatus()))
                .findFirst().orElse(null);
        if (Objects.isNull(evaluationCard) || Objects.isNull(evaluationCard.getFinalGrade())) {
            return "";
        }
        return String.valueOf(evaluationCard.getFinalGrade());
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.GRADES;
    }

    private String[] createHeaders() {
        return new String[]{
                CSVHeaders.INDEX,
                CSVHeaders.GRADE_FIRST_SEMESTER_FIRST_TERM,
                CSVHeaders.GRADE_FIRST_SEMESTER_SECOND_TERM,
                CSVHeaders.GRADE_SECOND_SEMESTER_FIRST_TERM,
                CSVHeaders.GRADE_SECOND_SEMESTER_SECOND_TERM
                };
    }

    private static class CSVHeaders {
        private static final String INDEX = "INDEKS";
        private static final String GRADE_FIRST_SEMESTER_FIRST_TERM = "OCENA I SEMESTR I TERMIN";
        private static final String GRADE_FIRST_SEMESTER_SECOND_TERM = "OCENA I SEMESTR II TERMIN";
        private static final String GRADE_SECOND_SEMESTER_FIRST_TERM = "OCENA II SEMESTR I TERMIN";
        private static final String GRADE_SECOND_SEMESTER_SECOND_TERM = "OCENA II SEMESTR II TERMIN";
    }
}
