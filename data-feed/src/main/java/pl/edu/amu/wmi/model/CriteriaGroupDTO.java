package pl.edu.amu.wmi.model;

import java.util.List;

public record CriteriaGroupDTO (
        Long id,
        String name,
        Double criteriaGroupGradeWeightFirstSemester,
        Double criteriaGroupGradeWeightSecondSemester,
        List<CriterionDTO> criteria
) {}
