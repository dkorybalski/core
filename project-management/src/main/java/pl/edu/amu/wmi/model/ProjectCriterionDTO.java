package pl.edu.amu.wmi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectCriterionDTO {

    private String category;

    private String description;

    private boolean isDisqualifying;

}
