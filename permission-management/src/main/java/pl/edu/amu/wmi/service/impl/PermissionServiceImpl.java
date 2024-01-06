package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.model.UserRoleType;
import pl.edu.amu.wmi.service.PermissionService;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.ACCEPTED;
import static pl.edu.amu.wmi.enumerations.UserRole.COORDINATOR;
import static pl.edu.amu.wmi.enumerations.UserRole.PROJECT_ADMIN;

@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final ProjectMemberServiceImpl projectMemberServiceImpl;

    private final ProjectDAO projectDAO;

    public PermissionServiceImpl(ProjectMemberServiceImpl projectMemberServiceImpl, ProjectDAO projectDAO) {
        this.projectMemberServiceImpl = projectMemberServiceImpl;
        this.projectDAO = projectDAO;
    }

    @Override
    public boolean isUserAllowedToSeeProjectDetails(String studyYear, String indexNumber, Long projectId) {
        Project project = projectDAO.findById(projectId).orElseThrow(() ->
                new BusinessException(MessageFormat.format("Project with id: {0} not found", projectId)));
        boolean isCoordinator = projectMemberServiceImpl.isUserRoleCoordinator(indexNumber);
        boolean isStudentAMemberOfProject = projectMemberServiceImpl.isStudentAMemberOfProject(indexNumber, project);
        boolean isSupervisorAllowedToSeeGrades = project.getEvaluationCards().stream()
                .anyMatch(evaluationCard -> isSupervisorAllowedToSeeGrades(project, evaluationCard, indexNumber));

        return isCoordinator || isStudentAMemberOfProject || isSupervisorAllowedToSeeGrades;
    }

    @Override
    public boolean isEvaluationCardEditableForUser(EvaluationCard evaluationCardEntity, Project project, String indexNumber) {
        if (!Objects.equals(AcceptanceStatus.ACCEPTED, project.getAcceptanceStatus())) {
            return false;
        }
        if (projectMemberServiceImpl.isUserAProjectSupervisor(project.getSupervisor(), indexNumber) && Objects.equals(EvaluationStatus.ACTIVE, evaluationCardEntity.getEvaluationStatus())) {
            return true;
        }
        if (isSupervisorAllowedToEditGrades(evaluationCardEntity, indexNumber)) {
            return true;
        }
        if (projectMemberServiceImpl.isUserRoleCoordinator(indexNumber)) {
            if (Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCardEntity.getEvaluationPhase())
                    && !Objects.equals(EvaluationStatus.ACTIVE, evaluationCardEntity.getEvaluationStatus())) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEvaluationCardVisibleForUser(EvaluationCard evaluationCardEntity, Project project, String indexNumber) {
        return projectMemberServiceImpl.isUserRoleCoordinator(indexNumber)
                || projectMemberServiceImpl.isStudentAMemberOfProject(indexNumber, project)
                || isSupervisorAllowedToSeeGrades(project, evaluationCardEntity, indexNumber);
    }

    private boolean isSupervisorAllowedToEditGrades(EvaluationCard evaluationCardEntity, String indexNumber) {
        return Objects.equals(UserRole.SUPERVISOR, projectMemberServiceImpl.getUserRoleByUserIndex(indexNumber, UserRoleType.BASE))
                && !Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCardEntity.getEvaluationPhase())
                && Objects.equals(EvaluationStatus.ACTIVE, evaluationCardEntity.getEvaluationStatus());
    }

    private boolean isSupervisorAllowedToSeeGrades(Project project, EvaluationCard evaluationCardEntity, String indexNumber) {
        boolean isUserAProjectSupervisor = projectMemberServiceImpl.isUserAProjectSupervisor(project.getSupervisor(), indexNumber);
        boolean isUserASupervisorAndProjectPhaseIsDifferentThanSemester = Objects.equals(UserRole.SUPERVISOR, projectMemberServiceImpl.getUserRoleByUserIndex(indexNumber, UserRoleType.BASE))
                && !Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCardEntity.getEvaluationPhase());

        return isUserAProjectSupervisor || isUserASupervisorAndProjectPhaseIsDifferentThanSemester;
    }

    @Override
    public boolean validateDeletionPermission(String userIndexNumber, Project project) {
        UserData userDataEntity = projectMemberServiceImpl.findUserDataByIndexNumber(userIndexNumber);
        List<UserRole> userRoles = userDataEntity.getRoles().stream()
                .map(Role::getName)
                .toList();
        if (userRoles.contains(COORDINATOR)) {
            return true;
        } else if (userRoles.contains(PROJECT_ADMIN)) {
            if (ACCEPTED == project.getAcceptanceStatus()) {
                return false;
            } else return projectMemberServiceImpl.isStudentAnAdminOfTheProject(userIndexNumber, project.getId());
        }
        return false;
    }

    @Override
    public boolean isProjectDefenseEditableForProjectAdmin(ProjectDefense projectDefense, String indexNumber, Project project) {
        if (Objects.isNull(project)) {
            return false;
        } else {
            Supervisor supervisor = project.getSupervisor();
            return isProjectAccepted(project) && isProjectSupervisorCommitteeMember(projectDefense, supervisor) &&
                    (isProjectDefenseSlotFree(projectDefense));
        }
    }

    private boolean isProjectAccepted(Project project) {
        return Objects.equals(ACCEPTED, project.getAcceptanceStatus());
    }

    private boolean isProjectDefenseSlotAssignToUserProject(Project project, ProjectDefense projectDefense) {
        return Objects.equals(project.getId(), projectDefense.getProject().getId());
    }

    private boolean isProjectDefenseSlotFree(ProjectDefense projectDefense) {
        return Objects.isNull(projectDefense.getProject());
    }

    private boolean isProjectSupervisorCommitteeMember(ProjectDefense projectDefense, Supervisor supervisor) {
        List<Supervisor> committeeMembers = projectDefense.getSupervisorDefenseAssignments().stream().map(SupervisorDefenseAssignment::getSupervisor).toList();
        return committeeMembers.contains(supervisor);
    }
}
