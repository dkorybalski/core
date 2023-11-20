package pl.edu.amu.wmi.model.grade;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CriteriaSectionDTO {

    private Long id;

    private String name;

    private String gradeWeight;

    private List<CriteriaGroupDTO> criteriaGroups;

}
