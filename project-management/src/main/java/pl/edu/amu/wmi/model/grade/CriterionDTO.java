package pl.edu.amu.wmi.model.grade;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CriterionDTO {

    private String description;

    @JsonProperty("isDisqualifying")
    private boolean isDisqualifying;

}
