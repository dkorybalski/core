package pl.edu.amu.wmi.enumerations;

public enum Semester {
    SEMESTER_I, SEMESTER_II;

    public static Semester getByShortSemesterName(String shortName) {
        return switch (shortName) {
            case "FIRST" -> SEMESTER_I;
            case "SECOND" -> SEMESTER_II;
            default -> throw new IllegalArgumentException("Incorrect semester value: " + shortName);
        };
    }
}
