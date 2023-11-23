package pl.edu.amu.wmi.service.grade;

public interface EvaluationCardPermissionService {


    boolean isUserAllowedToSeeEvaluationDetails(String studyYear, String username);
}
