package pl.edu.amu.wmi.model;

import java.util.List;

public record CriteriaSectionDTO(
        Long id,
        String name,
        Double criteriaSectionGradeWeightFirstSemester,
        Double criteriaSectionGradeWeightSecondSemester,
        List<CriteriaGroupDTO> criteriaGroups
) {}
