package pl.edu.amu.wmi.model;

import java.util.List;

public record CriteriaGroupDTO(
        Long id,
        String name,
        Double gradeWeightFirstSemester,
        Double gradeWeightSecondSemester,
        List<CriterionDTO> criteria
) {
}
