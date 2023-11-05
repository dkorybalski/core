package pl.edu.amu.wmi.model;

public record CriterionDTO(
        Long id,
        String category,
        Integer points,
        String description,
        boolean isDisqualifying
) {
}
