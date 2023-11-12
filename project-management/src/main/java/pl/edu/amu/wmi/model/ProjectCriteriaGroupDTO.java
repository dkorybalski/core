package pl.edu.amu.wmi.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ProjectCriteriaGroupDTO {

    @NotNull
    private Long id;

    private String name;

    private Integer selectedCriterion;

    private String gradeWeight;

    private LocalDate modificationDate;

    private List<ProjectCriterionDTO> criteria;

}
