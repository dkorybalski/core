package pl.edu.amu.wmi.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.model.UserRoleType;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private ProjectMemberServiceImpl projectMemberServiceImpl;

    @Mock
    private ProjectDAO projectDAO;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    void isEvaluationCardEditable_ProjectSupervisorAndEvaluationStatusActiveAndProjectAccepted() {
        //given
        String supervisorIndexNumber = "SUPERVISOR_1";
        Supervisor supervisor = createSupervisor(supervisorIndexNumber);
        EvaluationCard evaluationCard = createEvaluationCard(EvaluationStatus.ACTIVE, null);
        Project project = createProject(AcceptanceStatus.ACCEPTED, supervisor);
        Mockito.when(projectMemberServiceImpl.isUserAProjectSupervisor(project.getSupervisor(), supervisorIndexNumber)).thenReturn(true);
        //when
        boolean isEditable = this.permissionService.isEvaluationCardEditableForUser(evaluationCard, project, supervisorIndexNumber);
        //then
        assertTrue(isEditable);
    }

    @Test
    void isEvaluationCardEditable_NotProjectSupervisorAndEvaluationStatusActiveAndProjectAcceptedAndDefensePhase() {
        //given
        String supervisorIndexNumber = "SUPERVISOR_1";
        Supervisor supervisor = createSupervisor(supervisorIndexNumber);
        EvaluationCard evaluationCard = createEvaluationCard(EvaluationStatus.ACTIVE, EvaluationPhase.DEFENSE_PHASE);
        Project project = createProject(AcceptanceStatus.ACCEPTED, supervisor);
        Mockito.when(projectMemberServiceImpl.getUserRoleByUserIndex(supervisorIndexNumber, UserRoleType.BASE)).thenReturn(UserRole.SUPERVISOR);
        //when
        boolean isEditable = this.permissionService.isEvaluationCardEditableForUser(evaluationCard, project, supervisorIndexNumber);
        //then
        assertTrue(isEditable);
    }

    @Test
    void isEvaluationCardEditable_CoordinatorAndEvaluationStatusActiveAndSemesterPhaseAndProjectAccepted() {
        //given
        String coordinatorIndexNumber = "COORDINATOR_1";
        EvaluationCard evaluationCard = createEvaluationCard(EvaluationStatus.ACTIVE, EvaluationPhase.SEMESTER_PHASE);
        Project project = createProject(AcceptanceStatus.ACCEPTED, null);
        Mockito.when(projectMemberServiceImpl.isUserRoleCoordinator(coordinatorIndexNumber)).thenReturn(true);
        //when
        boolean isEditable = this.permissionService.isEvaluationCardEditableForUser(evaluationCard, project, coordinatorIndexNumber);
        //then
        assertTrue(isEditable);
    }

    @Test
    void isEvaluationCardNotEditable_SupervisorAndProjectPending() {
        //given
        String supervisorIndexNumber = "SUPERVISOR_1";
        EvaluationCard evaluationCard = createEvaluationCard(null, null);
        Project project = createProject(AcceptanceStatus.PENDING, null);
        //when
        boolean isEditable = this.permissionService.isEvaluationCardEditableForUser(evaluationCard, project, supervisorIndexNumber);
        //then
        assertFalse(isEditable);
    }

    @Test
    void isEvaluationCardNotEditable_CoordinatorAndEvaluationStatusFrozenAndProjectAcceptedAndSemesterPhase() {
        //given
        String coordinatorIndexNumber = "COORDINATOR_1";
        EvaluationCard evaluationCard = createEvaluationCard(EvaluationStatus.FROZEN, EvaluationPhase.SEMESTER_PHASE);
        Project project = createProject(AcceptanceStatus.ACCEPTED, null);
        Mockito.when(projectMemberServiceImpl.isUserRoleCoordinator(coordinatorIndexNumber)).thenReturn(true);
        //when
        boolean isEditable = this.permissionService.isEvaluationCardEditableForUser(evaluationCard, project, coordinatorIndexNumber);
        //then
        assertFalse(isEditable);
    }

    @Test
    void isEvaluationCardNotEditableForUser_StudentAndAcceptanceStatusAccepted() {
        //given
        String studentIndexNumber = "STUDENT_1";
        EvaluationCard evaluationCard = new EvaluationCard();
        Project project = createProject(AcceptanceStatus.ACCEPTED, null);
        Mockito.when(projectMemberServiceImpl.isUserRoleCoordinator(studentIndexNumber)).thenReturn(false);
        //when
        boolean isEditable = this.permissionService.isEvaluationCardEditableForUser(evaluationCard, project, studentIndexNumber);
        //then
        assertFalse(isEditable);
    }

    @Test
    void isEvaluationCardVisibleForUser_Coordinator() {
        //given
        String coordinatorIndexNumber = "COORDINATOR_1";
        Project project = new Project();
        EvaluationCard evaluationCard = new EvaluationCard();
        Mockito.when(projectMemberServiceImpl.isUserRoleCoordinator(coordinatorIndexNumber)).thenReturn(true);
        //when
        boolean isVisible = this.permissionService.isEvaluationCardVisibleForUser(evaluationCard, project, coordinatorIndexNumber);
        //then
        assertTrue(isVisible);
    }

    @Test
    void isEvaluationCardVisibleForUser_ProjectMember() {
        //given
        String studentIndexNumber = "STUDENT_1";
        Project project = new Project();
        EvaluationCard evaluationCard = new EvaluationCard();
        Mockito.when(projectMemberServiceImpl.isUserRoleCoordinator(studentIndexNumber)).thenReturn(false);
        Mockito.when(projectMemberServiceImpl.isStudentAMemberOfProject(studentIndexNumber, project)).thenReturn(true);
        //when
        boolean isVisible = this.permissionService.isEvaluationCardVisibleForUser(evaluationCard, project, studentIndexNumber);
        //then
        assertTrue(isVisible);
    }

    @Test
    void isEvaluationCardVisibleForUser_ProjectSupervisor() {
        //given
        String supervisorIndexNumber = "SUPERVISOR_1";
        Project project = new Project();
        EvaluationCard evaluationCard = new EvaluationCard();
        Mockito.when(projectMemberServiceImpl.isUserRoleCoordinator(supervisorIndexNumber)).thenReturn(false);
        Mockito.when(projectMemberServiceImpl.isStudentAMemberOfProject(supervisorIndexNumber, project)).thenReturn(false);
        Mockito.when(projectMemberServiceImpl.isUserAProjectSupervisor(project.getSupervisor(), supervisorIndexNumber)).thenReturn(true);
        //when
        boolean isVisible = this.permissionService.isEvaluationCardVisibleForUser(evaluationCard, project, supervisorIndexNumber);
        //then
        assertTrue(isVisible);
    }

    @Test
    void isEvaluationCardNotVisibleForUser_NotProjectMember() {
        //given
        String studentIndexNumber = "STUDENT_1";
        Project project = new Project();
        EvaluationCard evaluationCard = new EvaluationCard();
        Mockito.when(projectMemberServiceImpl.isUserRoleCoordinator(studentIndexNumber)).thenReturn(false);
        Mockito.when(projectMemberServiceImpl.isStudentAMemberOfProject(studentIndexNumber, project)).thenReturn(false);
        //when
        boolean isVisible = this.permissionService.isEvaluationCardVisibleForUser(evaluationCard, project, studentIndexNumber);
        //then
        assertFalse(isVisible);
    }

    @Test
    void hasDeletionPermission_Coordinator() {
        //given
        String coordinatorIndexNumber = "COORDINATOR_1";
        Project project = new Project();
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(createUserRole(UserRole.COORDINATOR));
        UserData coordinatorUserData = createUserData(userRoles);
        Mockito.when(projectMemberServiceImpl.findUserDataByIndexNumber(coordinatorIndexNumber)).thenReturn(coordinatorUserData);
        //when
        boolean isVisible = this.permissionService.validateDeletionPermission(coordinatorIndexNumber, project);
        //then
        assertTrue(isVisible);
    }

    @Test
    void hasDeletionPermission_ProjectAdminProjectNotAccepted() {
        //given
        String studentIndexNumber = "STUDENT_1";
        Project project = createProject(AcceptanceStatus.PENDING, new Supervisor());
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(createUserRole(UserRole.PROJECT_ADMIN));
        UserData studentUserData = createUserData(userRoles);
        Mockito.when(projectMemberServiceImpl.findUserDataByIndexNumber(studentIndexNumber)).thenReturn(studentUserData);
        Mockito.when(projectMemberServiceImpl.isStudentAnAdminOfTheProject(studentIndexNumber, project.getId())).thenReturn(true);
        //when
        boolean isVisible = this.permissionService.validateDeletionPermission(studentIndexNumber, project);
        //then
        assertTrue(isVisible);
    }

    @Test
    void hasNotDeletionPermission_ProjectAdminProjectAccepted() {
        //given
        String studentIndexNumber = "STUDENT_1";
        Project project = createProject(AcceptanceStatus.ACCEPTED, new Supervisor());
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(createUserRole(UserRole.PROJECT_ADMIN));
        UserData studentUserData = createUserData(userRoles);
        Mockito.when(projectMemberServiceImpl.findUserDataByIndexNumber(studentIndexNumber)).thenReturn(studentUserData);
        //when
        boolean isVisible = this.permissionService.validateDeletionPermission(studentIndexNumber, project);
        //then
        assertFalse(isVisible);
    }

    @Test
    void hasNotDeletionPermission_Supervisor() {
        //given
        String supervisorIndexNumber = "SUPERVISOR_1";
        Project project = new Project();
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(createUserRole(UserRole.SUPERVISOR));
        UserData supervisorUserData = createUserData(userRoles);
        Mockito.when(projectMemberServiceImpl.findUserDataByIndexNumber(supervisorIndexNumber)).thenReturn(supervisorUserData);
        //when
        boolean isVisible = this.permissionService.validateDeletionPermission(supervisorIndexNumber, project);
        //then
        assertFalse(isVisible);
    }

    private Project createProject(AcceptanceStatus acceptanceStatus, Supervisor supervisor) {
        Project project = new Project();
        project.setId(1L);
        project.setAcceptanceStatus(acceptanceStatus);
        project.setSupervisor(supervisor);
        return project;
    }

    private EvaluationCard createEvaluationCard(EvaluationStatus evaluationStatus, EvaluationPhase evaluationPhase) {
        EvaluationCard evaluationCard = new EvaluationCard();
        evaluationCard.setEvaluationStatus(evaluationStatus);
        evaluationCard.setEvaluationPhase(evaluationPhase);
        return evaluationCard;
    }

    private Supervisor createSupervisor(String supervisorIndexNumber) {
        Supervisor supervisor = new Supervisor();
        UserData userData = new UserData();
        userData.setIndexNumber(supervisorIndexNumber);
        supervisor.setUserData(userData);
        supervisor.setMaxNumberOfProjects(3);
        supervisor.setProjects(new HashSet<>());
        return supervisor;
    }

    private UserData createUserData(Set<Role> userRoles) {
        UserData userData = new UserData();
        userData.setRoles(userRoles);
        return userData;
    }

    private Role createUserRole(UserRole roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }
}