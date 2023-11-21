package pl.edu.amu.wmi.model.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;

import java.util.Set;


@Data
@NoArgsConstructor
public class ProjectDTO {

    private Long id;

    private String name;

    private SupervisorDTO supervisor;

    private boolean accepted;

    @JsonProperty("firstSemesterGrade")
    private String pointsFirstSemester;

    @JsonProperty("secondSemesterGrade")
    private String pointsSecondSemester;

    private Boolean criteriaMet;

    private Set<ExternalLinkDTO> externalLinks;

}
