package pl.edu.amu.wmi.model.grade;

import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.Semester;

import java.util.Map;

public record EvaluationCardDTO (
        Map<Semester, Map<EvaluationPhase, EvaluationCardDetails>> evaluationCardDTO
) {}
