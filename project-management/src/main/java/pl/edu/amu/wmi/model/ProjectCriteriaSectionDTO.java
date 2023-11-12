package pl.edu.amu.wmi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ProjectCriteriaSectionDTO {

    private Long id;

    private String name;

    private String gradeWeight;

    private List<ProjectCriteriaGroupDTO> criteriaGroups;

}
