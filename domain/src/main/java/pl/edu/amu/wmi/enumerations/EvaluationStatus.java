package pl.edu.amu.wmi.enumerations;

public enum EvaluationStatus {

    ACTIVE("Active"), FROZEN("Hidden"), PUBLISHED("Published"), RETAKE("Retake"), INACTIVE("Inactive");

    public final String label;

    EvaluationStatus(String label) {
        this.label = label;
    }

}
