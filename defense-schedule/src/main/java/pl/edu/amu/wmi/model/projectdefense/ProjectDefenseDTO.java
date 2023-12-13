package pl.edu.amu.wmi.model.projectdefense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProjectDefenseDTO {

    private Long projectDefenseId;

    private Long projectId;

    /**
     * defense time send as string with format 7:00 - 7:30
     */
    private String time;

    private String projectName;

    @JsonProperty("classRoom")
    private String classroom;

    /**
     * names (first name and last name) of committee members
     */
    private List<String> committee;

    /**
     * name (first name and last name) of chairperson
     */
    private String chairperson;

    /**
     * value depends on the project defense phase and on the user role
     */
    private boolean isEditable;
}
