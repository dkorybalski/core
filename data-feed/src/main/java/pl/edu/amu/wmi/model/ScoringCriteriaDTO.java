package pl.edu.amu.wmi.model;

public record ScoringCriteriaDTO(
        Long id,
        String category,
        Integer points,
        String description,
        boolean isDisqualifying
) {
}
