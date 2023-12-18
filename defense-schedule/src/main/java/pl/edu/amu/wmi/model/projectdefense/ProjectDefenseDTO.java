package pl.edu.amu.wmi.model.projectdefense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;

@Data
@NoArgsConstructor
public class ProjectDefenseDTO {

    private Long projectDefenseId;

    private String projectId;

    private String date;

    /**
     * defense start time send as string with format 7:00
     */
    private String time;

    private String projectName;

    @JsonProperty("classRoom")
    private String classroom;

    /**
     * names initials of committee members
     */
    private String committee;

    private CommitteeIdentifier committeeIdentifier;

    private String students;

    /**
     * name initials of chairperson
     */
    private String chairperson;

    /**
     * value depends on the project defense phase and on the user role
     */
    @JsonProperty("isEditable")
    private boolean isEditable;
}
