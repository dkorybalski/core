package pl.edu.amu.wmi.model.projectdefense;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProjectDefenseSummaryDTO {

    /**
     * defense start time send as string with format 7:00
     */
    private String time;

    private String projectName;

    private String classroom;

    /**
     * names initials of committee members
     */
    private String committee;

    /**
     * name initials of chairperson
     */
    private String chairperson;

    private List<String> students;

    private String supervisor;
}
