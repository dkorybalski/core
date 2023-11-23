package pl.edu.amu.wmi.model.grade;

public record UpdatedGradeDTO(
        String grade,
        boolean criteriaMet
) {
}
