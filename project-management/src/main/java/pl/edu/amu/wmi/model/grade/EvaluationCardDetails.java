package pl.edu.amu.wmi.model.grade;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EvaluationCardDetails {

    @NotNull
    private Long id;

    private String grade;

    private boolean editable;

    // TODO: 11/22/2023 this field is probably not necessary
    private boolean visible;

    private List<CriteriaSectionDTO> sections;

}
