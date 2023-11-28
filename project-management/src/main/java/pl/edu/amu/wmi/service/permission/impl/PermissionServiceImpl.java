package pl.edu.amu.wmi.service.permission.impl;

import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.EvaluationCard;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.Role;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.service.permission.PermissionService;
import pl.edu.amu.wmi.service.project.ProjectMemberService;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.ACCEPTED;
import static pl.edu.amu.wmi.enumerations.UserRole.COORDINATOR;
import static pl.edu.amu.wmi.enumerations.UserRole.PROJECT_ADMIN;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final ProjectMemberService projectMemberService;

    private final ProjectDAO projectDAO;

    public PermissionServiceImpl(ProjectMemberService projectMemberService, ProjectDAO projectDAO) {
        this.projectMemberService = projectMemberService;
        this.projectDAO = projectDAO;
    }

    @Override
    public boolean isUserAllowedToSeeProjectDetails(String studyYear, String indexNumber, Long projectId) {
        Project project = projectDAO.findById(projectId).orElseThrow(() ->
                new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));
        boolean isCoordinator = projectMemberService.isUserRoleCoordinator(indexNumber);
        boolean isStudentAMemberOfProject = projectMemberService.isStudentAMemberOfProject(indexNumber, project);
        boolean isSupervisorAllowedToSeeGrades = project.getEvaluationCards().stream()
                .anyMatch(evaluationCard -> isSupervisorAllowedToSeeGrades(project, evaluationCard, indexNumber));

        return isCoordinator || isStudentAMemberOfProject || isSupervisorAllowedToSeeGrades;
    }

    @Override
    public boolean isEvaluationCardEditableForUser(EvaluationCard evaluationCardEntity, Project project, String indexNumber) {
        if (!Objects.equals(AcceptanceStatus.ACCEPTED, project.getAcceptanceStatus())) {
            return false;
        }
        if (projectMemberService.isUserAProjectSupervisor(project.getSupervisor(), indexNumber) && Objects.equals(EvaluationStatus.ACTIVE, evaluationCardEntity.getEvaluationStatus())) {
            return true;
        }
        if (isSupervisorAllowedToEditGrades(evaluationCardEntity, indexNumber)) {
            return true;
        }
        if (projectMemberService.isUserRoleCoordinator(indexNumber)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEvaluationCardVisibleForUser(EvaluationCard evaluationCardEntity, Project project, String indexNumber) {
        return projectMemberService.isUserRoleCoordinator(indexNumber)
                || projectMemberService.isStudentAMemberOfProject(indexNumber, project)
                || isSupervisorAllowedToSeeGrades(project, evaluationCardEntity, indexNumber);
    }

    private boolean isSupervisorAllowedToEditGrades(EvaluationCard evaluationCardEntity, String indexNumber) {
        return Objects.equals(UserRole.SUPERVISOR, projectMemberService.getUserRoleByUserIndex(indexNumber))
                && !Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCardEntity.getEvaluationPhase())
                && Objects.equals(EvaluationStatus.ACTIVE, evaluationCardEntity.getEvaluationStatus());
    }

    private boolean isSupervisorAllowedToSeeGrades(Project project, EvaluationCard evaluationCardEntity, String indexNumber) {
        boolean isUserAProjectSupervisor = projectMemberService.isUserAProjectSupervisor(project.getSupervisor(), indexNumber);
        boolean isUserASupervisorAndProjectPhaseIsDifferentThanSemester = Objects.equals(UserRole.SUPERVISOR, projectMemberService.getUserRoleByUserIndex(indexNumber))
                && !Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCardEntity.getEvaluationPhase());

        return isUserAProjectSupervisor || isUserASupervisorAndProjectPhaseIsDifferentThanSemester;
    }

    @Override
    public boolean validateDeletionPermission(String userIndexNumber, Project project) {
        UserData userDataEntity = projectMemberService.findUserDataByIndexNumber(userIndexNumber);
        List<UserRole> userRoles = userDataEntity.getRoles().stream()
                .map(Role::getName)
                .toList();
        if (userRoles.contains(COORDINATOR)) {
            return true;
        } else if (userRoles.contains(PROJECT_ADMIN)) {
            if (ACCEPTED == project.getAcceptanceStatus()) {
                return false;
            } else return projectMemberService.isStudentAnAdminOfTheProject(userIndexNumber, project.getId());
        }
        return false;
    }

}
