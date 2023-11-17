package pl.edu.amu.wmi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectCriterionDTO {

    private String description;

    @JsonProperty("isDisqualifying")
    private boolean isDisqualifying;

}
