package pl.edu.amu.wmi.model.grade;

import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.Semester;

import java.util.Map;

public record EvaluationCardsDTO (
        Map<Semester, Map<EvaluationPhase, EvaluationCardDetailsDTO>> evaluationCards,
        String phase
) {
}
