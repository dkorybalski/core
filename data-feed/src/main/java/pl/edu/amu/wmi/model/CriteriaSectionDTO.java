package pl.edu.amu.wmi.model;

import java.util.List;

public record CriteriaSectionDTO(
        Long idFirstSemester,
        Long idSecondSemester,
        String name,
        boolean isDefenseSection,
        Double criteriaSectionGradeWeightFirstSemester,
        Double criteriaSectionGradeWeightSecondSemester,
        List<CriteriaGroupDTO> criteriaGroups
) {}
