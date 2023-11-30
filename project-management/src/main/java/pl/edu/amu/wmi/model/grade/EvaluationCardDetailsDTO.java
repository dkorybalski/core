package pl.edu.amu.wmi.model.grade;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EvaluationCardDetailsDTO {

    @NotNull
    private Long id;

    private String grade;

    private boolean editable;

    private List<CriteriaSectionDTO> sections;

}
