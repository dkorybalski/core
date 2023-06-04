package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.mapper.UserMapper;
import pl.edu.amu.wmi.model.user.UserDTO;
import pl.edu.amu.wmi.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDataDAO userDataDAO;

    private final StudentDAO studentDAO;

    private final SupervisorDAO supervisorDAO;

    private final UserMapper userMapper;

    public UserServiceImpl(UserDataDAO userDataDAO, StudentDAO studentDAO, SupervisorDAO supervisorDAO, UserMapper userMapper) {
        this.userDataDAO = userDataDAO;
        this.studentDAO = studentDAO;
        this.supervisorDAO = supervisorDAO;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO getUser(String indexNumber) {
        // TODO: 6/4/2023 solve issue when user is added to database many times with different studyYears
        // probably redesign in the database to store user only once in the db
        Optional<UserData> userData = userDataDAO.findByIndexNumber(indexNumber);
        if (userData.isEmpty()) {
            throw new UsernameNotFoundException("User with indexNumber not found: " + indexNumber);
        }
        UserDTO userDTO = userMapper.mapToDto(userData.get());
        String roleWithTheHighestPermissions = findRoleWithTheHighestPermissions(userData.get().getRoles());
        userDTO.setRole(roleWithTheHighestPermissions);

        List<String> acceptedProjects = new ArrayList<>();
        List<String> assignedProjects = new ArrayList<>();

        if (hasRoleStudent(userData.get().getRoles())) {
            Student entity = studentDAO.findByUserData_IndexNumber(indexNumber);
            Project acceptedProject = entity.getConfirmedProject();

            if (acceptedProject != null)
                acceptedProjects.add(acceptedProject.getId().toString());

            assignedProjects = entity.getAssignedProjects().stream()
                    .map(studentProject -> studentProject.getProject().getId().toString()).toList();
        }

        if (hasRoleSupervisor(userData.get().getRoles())) {
            Supervisor entity = supervisorDAO.findByUserData_IndexNumber(indexNumber);
            acceptedProjects = entity.getProjects().stream()
                    .filter(project -> project.getAcceptanceStatus().equals(AcceptanceStatus.ACCEPTED))
                    .map(project -> project.getId().toString()).toList();
            assignedProjects = entity.getProjects().stream()
                    .map(project -> project.getId().toString()).toList();
        }

        userDTO.setAcceptedProjects(acceptedProjects);
        userDTO.setProjects(assignedProjects);

        return userDTO;
    }

    private String findRoleWithTheHighestPermissions(Set<Role> roles) {
        List<UserRole> roleNames = extractRoleNames(roles);
        return roleNames.contains(UserRole.COORDINATOR) ? UserRole.COORDINATOR.name() :
                roleNames.contains(UserRole.SUPERVISOR) ? UserRole.SUPERVISOR.name() :
                        roleNames.contains(UserRole.PROJECT_ADMIN) ? UserRole.PROJECT_ADMIN.name() : UserRole.STUDENT.name();
    }

    private boolean hasRoleStudent(Set<Role> roles) {
        List<UserRole> roleNames = extractRoleNames(roles);
        return roleNames.contains(UserRole.STUDENT) || roleNames.contains(UserRole.PROJECT_ADMIN);
    }

    private boolean hasRoleSupervisor(Set<Role> roles) {
        List<UserRole> roleNames = extractRoleNames(roles);
        return roleNames.contains(UserRole.SUPERVISOR);
    }

    private List<UserRole> extractRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

}
