package pl.edu.amu.wmi.model;

import java.util.List;

public record CriterionDTO(
        Long id,
        String name,
        Double gradeWeightFirstSemester,
        Double gradeWeightSecondSemester,
        List<ScoringCriteriaDTO> scoringCriteria
) {
}
