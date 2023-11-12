package pl.edu.amu.wmi.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.Semester;

import java.util.List;

@Data
@NoArgsConstructor
public class ProjectGradeDetailsDTO {

    @NotNull
    private Long id;

    private String projectName;

    private Semester semester;

    private String grade;

    private List<ProjectCriteriaSectionDTO> sections;

}
