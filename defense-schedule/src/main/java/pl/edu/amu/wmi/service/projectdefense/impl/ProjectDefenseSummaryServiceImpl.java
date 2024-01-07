package pl.edu.amu.wmi.service.projectdefense.impl;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseSummaryDTO;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseSummaryService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProjectDefenseSummaryServiceImpl implements ProjectDefenseSummaryService {

    private final ProjectDefenseService projectDefenseService;

    public ProjectDefenseSummaryServiceImpl(ProjectDefenseService projectDefenseService) {
        this.projectDefenseService = projectDefenseService;
    }

    @Override
    public void exportDefenseScheduleSummaryData(PrintWriter writer, String studyYear) throws CsvException {
        try (CSVWriter csvWriter = new CSVWriter(writer, ';', ICSVWriter.NO_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END);) {
            csvWriter.writeNext(createHeaders());
            Map<String, List<ProjectDefenseSummaryDTO>> projectDefensesSummary = projectDefenseService.getProjectDefensesSummary(studyYear);

            projectDefensesSummary.forEach((date, defenses) ->
                defenses.forEach(defense -> csvWriter.writeNext(createDefenseSummaryData(date, defense)))
            );

        } catch (IOException e) {
            log.error("Error during parsing csv file with project summary data", e);
            throw new CsvException();
        }
    }

    private String[] createDefenseSummaryData(String date, ProjectDefenseSummaryDTO defense) {
        String committeeMembers = String.join(", ", defense.getCommittee());
        String students = String.join(", ", defense.getStudents());
        return new String[]{
                date,
                defense.getTime(),
                defense.getClassroom(),
                committeeMembers,
                defense.getProjectName(),
                defense.getSupervisor(),
                students
        };
    }

    private String[] createHeaders() {
        return new String[]{CSVHeaders.DATE,
                CSVHeaders.TIME,
                CSVHeaders.CLASSROOM,
                CSVHeaders.COMMITTEE_MEMBERS,
                CSVHeaders.PROJECT_NAME,
                CSVHeaders.SUPERVISOR,
                CSVHeaders.STUDENTS};
    }

    private static class CSVHeaders {
        private static final String DATE = "DATA";
        private static final String PROJECT_NAME = "NAZWA PROJEKTU";
        private static final String TIME = "GODZINA";
        private static final String CLASSROOM = "SALA";
        private static final String COMMITTEE_MEMBERS = "CZŁONKOWIE KOMISJI";
        private static final String SUPERVISOR = "OPIEKUN PROJEKTU";
        private static final String STUDENTS = "STUDENCI";
    }
}
