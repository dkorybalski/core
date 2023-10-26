package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.UserManagementException;
import pl.edu.amu.wmi.mapper.UserMapper;
import pl.edu.amu.wmi.model.user.UserDTO;
import pl.edu.amu.wmi.service.UserService;

import java.util.*;
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
    public UserDTO getUser(String indexNumber, String studyYear) {
        try {
            final Optional<UserData> userData = this.userDataDAO.findByIndexNumber(indexNumber);
            if (userData.isEmpty()) {
                throw new UsernameNotFoundException("User with indexNumber not found: " + indexNumber);
            }

            final UserDTO userDTO = this.userMapper.mapToDto(userData.get());
            final String roleWithTheHighestPermissions = findRoleWithTheHighestPermissions(userData.get().getRoles());
            userDTO.setRole(roleWithTheHighestPermissions);

            final List<Long> acceptedProjects = new ArrayList<>();
            final List<Long> assignedProjects = new ArrayList<>();
            final List<String> studyYears = new ArrayList<>();

            if (hasRoleStudent(userData.get().getRoles())) {
                final List<Student> students = this.studentDAO.findAllByUserData_IndexNumber(indexNumber);

                final List<String> studentStudyYears = getStudyYearsForStudent(students);
                studyYears.addAll(studentStudyYears);

                final String actualStudyYear = getActualStudyYear(studyYear, studentStudyYears);
                userDTO.setActualYear(actualStudyYear);

                final Student entity = findStudentByActualStudyYear(students, actualStudyYear, indexNumber);
                if (isIncorrectUserRoleForStudyYear(entity, userDTO)) {
                    userDTO.setRole(UserRole.STUDENT.name());
                }

                final Project acceptedProject = entity.getConfirmedProject();
                if (acceptedProject != null) {
                    acceptedProjects.add(acceptedProject.getId());
                }
                assignedProjects.addAll(getStudentAssignedProjects(entity));
            }

            if (hasRoleSupervisor(userData.get().getRoles())) {
                final List<Supervisor> supervisors = this.supervisorDAO.findAllByUserData_IndexNumber(indexNumber);

                final List<String> supervisorStudyYears = getStudyYearsForSupervisor(supervisors);
                studyYears.addAll(supervisorStudyYears);

                final String actualStudyYear = getActualStudyYear(studyYear, supervisorStudyYears);
                userDTO.setActualYear(actualStudyYear);

                final Supervisor entity = findSupervisorByActualStudyYear(supervisors, actualStudyYear, indexNumber);
                acceptedProjects.addAll(getSupervisorAcceptedProjects(entity));
                assignedProjects.addAll(getSupervisorAssignedProjects(entity));
            }

            userDTO.setStudyYears(studyYears);
            userDTO.setAcceptedProjects(acceptedProjects);
            userDTO.setProjects(assignedProjects);
            log.info("User successfully fetched: {}", userDTO);
            return userDTO;

        } catch (Exception exception) {
            log.error("Exception during fetching the user data with index number: {} and study year: {}", indexNumber, studyYear, exception);
            throw exception;
        }
    }

    private List<Long> getSupervisorAssignedProjects(Supervisor entity) {
        return entity.getProjects().stream()
                .map(BaseAbstractEntity::getId).toList();
    }

    private List<Long> getSupervisorAcceptedProjects(Supervisor entity) {
        return entity.getProjects().stream()
                .filter(project -> project.getAcceptanceStatus().equals(AcceptanceStatus.ACCEPTED))
                .map(BaseAbstractEntity::getId).toList();
    }

    private List<Long> getStudentAssignedProjects(Student entity) {
        return entity.getAssignedProjects().stream()
                .map(studentProject -> studentProject.getProject().getId()).toList();
    }

    private Supervisor findSupervisorByActualStudyYear(List<Supervisor> supervisors, String actualStudyYear, String indexNumber) {
        return supervisors.stream()
                .filter(supervisor -> Objects.equals(supervisor.getStudyYear(), actualStudyYear))
                .findFirst().orElseThrow(() -> new UserManagementException(
                        "Supervisor with index: " + indexNumber + " not found for study year: " + actualStudyYear));
    }

    private List<String> getStudyYearsForSupervisor(List<Supervisor> supervisors) {
        return supervisors.stream()
                .map(Supervisor::getStudyYear)
                .toList();
    }

    private List<String> getStudyYearsForStudent(List<Student> students) {
        return students.stream()
                .map(Student::getStudyYear)
                .toList();
    }

    private String getActualStudyYear(String studyYear, List<String> studyYears) {
        return (studyYear == null || studyYear.isEmpty())
                ? findTheMostRecentStudyYear(studyYears) : studyYear;
    }

    private Student findStudentByActualStudyYear(List<Student> students, String actualStudyYear, String indexNumber) {
        return students.stream()
                .filter(student -> Objects.equals(student.getStudyYear(), actualStudyYear))
                .findFirst().orElseThrow(() -> new UserManagementException(
                        "Student with index: " + indexNumber + " not found for study year: " + actualStudyYear));
    }

    private boolean isIncorrectUserRoleForStudyYear(Student entity, UserDTO userDTO) {
        return !entity.isProjectAdmin() && Objects.equals(UserRole.PROJECT_ADMIN.name(), userDTO.getRole());
    }

    private String findTheMostRecentStudyYear(List<String> studyYears) {
        Comparator<String> byYearAndType = createStudyYearComparatorByYearAndType();
        List<String> sortedStudyYears = new ArrayList<>(studyYears);
        sortedStudyYears.sort(byYearAndType);
        return sortedStudyYears.get(0);
    }

    private Comparator<String> createStudyYearComparatorByYearAndType() {
        return Comparator.comparing((String studyYear) -> StringUtils.substringAfter(studyYear, "#"), Comparator.reverseOrder())
                .thenComparing((String studyYear) -> StringUtils.substringBefore(studyYear, "#"));
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