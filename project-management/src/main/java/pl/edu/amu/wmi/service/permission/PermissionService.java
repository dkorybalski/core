package pl.edu.amu.wmi.service.permission;

import pl.edu.amu.wmi.entity.EvaluationCard;
import pl.edu.amu.wmi.entity.Project;

public interface PermissionService {
    boolean isEvaluationCardEditableForUser(EvaluationCard evaluationCardEntity, Project project, String indexNumber);
    boolean isEvaluationCardVisibleForUser(EvaluationCard evaluationCardEntity, Project project, String indexNumber);
    boolean isUserAllowedToSeeProjectDetails(String studyYear, String username, Long projectId);
    boolean validateDeletionPermission(String userIndexNumber, Project project);
}
