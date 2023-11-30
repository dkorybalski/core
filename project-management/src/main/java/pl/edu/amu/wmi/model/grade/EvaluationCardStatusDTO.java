package pl.edu.amu.wmi.model.grade;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;

@Data
@NoArgsConstructor
public class EvaluationCardStatusDTO {

    @NotNull
    private Long id;

    private EvaluationStatus evaluationStatus;

}
