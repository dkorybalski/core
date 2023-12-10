package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.model.UserRoleType;
import pl.edu.amu.wmi.service.ProjectMemberService;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static pl.edu.amu.wmi.enumerations.UserRole.*;

@Service
@Slf4j
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final UserDataDAO userDataDAO;
    private final StudentDAO studentDAO;

    public ProjectMemberServiceImpl(UserDataDAO userDataDAO, StudentDAO studentDAO) {
        this.userDataDAO = userDataDAO;
        this.studentDAO = studentDAO;
    }

    @Override
    public UserRole getUserRoleByUserIndex(String index, UserRoleType userRoleType) {
        UserData userData = userDataDAO.findByIndexNumber(index).orElseThrow(()
                -> new BusinessException(MessageFormat.format("User with index: {0} not found.", index)));
        if (Objects.equals(UserRoleType.BASE, userRoleType)) {
            Role userRole = userData.getRoles().stream()
                    .filter(role -> role.getName().equals(STUDENT) || role.getName().equals(SUPERVISOR))
                    .findFirst().orElseThrow(()
                            -> new BusinessException(MessageFormat.format("User with index: {0} does not have required role.", index)));
            return userRole.getName();
        } else {
            return findRoleWithTheHighestPermissions(userData.getRoles());
        }
    }

    @Override
    public boolean isUserRoleCoordinator(String index) {
        UserData userData = userDataDAO.findByIndexNumber(index).orElseThrow(()
                -> new BusinessException(MessageFormat.format("User with index: {0} not found.", index)));
        return userData.getRoles().stream().anyMatch(role -> role.getName().equals(COORDINATOR));
    }

    @Override
    public UserData findUserDataByIndexNumber(String indexNumber) {
        return userDataDAO.findByIndexNumber(indexNumber).orElseThrow(()
                -> new BusinessException(MessageFormat.format("User with index: {0} not found", indexNumber)));
    }

    @Override
    public boolean isStudentAMemberOfProject(String indexNumber, Project project) {
        return project.getStudents().stream()
                .map(Student::getIndexNumber)
                .anyMatch(studentIndexId -> Objects.equals(indexNumber, studentIndexId));
    }

    @Override
    public boolean isUserAProjectSupervisor(Supervisor supervisor, String indexNumber) {
        return Objects.equals(supervisor.getIndexNumber(), indexNumber);
    }

    @Override
    public boolean isStudentAnAdminOfTheProject(String userIndexNumber, Long projectId) {
        Student student = studentDAO.findByUserData_IndexNumber(userIndexNumber);
        return Objects.equals(student.getConfirmedProject().getId(), projectId) &&
                student.isProjectAdmin();
    }

    private UserRole findRoleWithTheHighestPermissions(Set<Role> roles) {
        List<UserRole> roleNames = extractRoleNames(roles);
        return roleNames.contains(UserRole.COORDINATOR) ? UserRole.COORDINATOR :
                roleNames.contains(UserRole.SUPERVISOR) ? UserRole.SUPERVISOR :
                        roleNames.contains(UserRole.PROJECT_ADMIN) ? UserRole.PROJECT_ADMIN : UserRole.STUDENT;
    }

    private List<UserRole> extractRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .toList();
    }
}
