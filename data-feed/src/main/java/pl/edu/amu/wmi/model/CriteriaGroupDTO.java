package pl.edu.amu.wmi.model;

import java.util.List;

public record CriteriaGroupDTO(
        Long idFirstSemester,
        Long idSecondSemester,
        String name,
        Double gradeWeightFirstSemester,
        Double gradeWeightSecondSemester,
        List<CriterionDTO> criteria
) {
}
