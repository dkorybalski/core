package pl.edu.amu.wmi.model.projectdefense;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProjectDefenseSummaryDTO {

    /**
     * defense time send as string with format 7:00 - 7:30
     */
    private String time;

    private String projectName;

    private String classroom;

    /**
     * names (first name and last name) of committee members
     */
    private List<String> committee;

    /**
     * name (first name and last name) of chairperson
     */
    private String chairperson;

    private List<String> students;

    private String supervisor;
}
