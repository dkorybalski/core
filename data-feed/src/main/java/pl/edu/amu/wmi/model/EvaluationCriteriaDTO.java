package pl.edu.amu.wmi.model;

import java.util.List;

public record EvaluationCriteriaDTO(
        String studyYear,
        Double minPointsThresholdFirstSemester,
        Double minPointsThresholdSecondSemester,
        List<CriteriaSectionDTO> criteriaSections
) {
}
