package pl.edu.amu.wmi.model.projectdefense;

import lombok.Data;
import lombok.NoArgsConstructor;

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

    private String students;

    private String supervisor;
}
