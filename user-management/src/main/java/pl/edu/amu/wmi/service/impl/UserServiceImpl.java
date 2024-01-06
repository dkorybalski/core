package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.StudyYearDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.UserManagementException;
import pl.edu.amu.wmi.mapper.UserMapper;
import pl.edu.amu.wmi.model.user.UserDTO;
import pl.edu.amu.wmi.service.SessionDataService;
import pl.edu.amu.wmi.service.UserService;

import java.text.MessageFormat;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserDataDAO userDataDAO;

    private final StudentDAO studentDAO;

    private final SupervisorDAO supervisorDAO;
    private final StudyYearDAO studyYearDAO;

    private final UserMapper userMapper;

    private final SessionDataService sessionDataService;


    public UserServiceImpl(UserDataDAO userDataDAO, StudentDAO studentDAO, SupervisorDAO supervisorDAO, StudyYearDAO studyYearDAO, UserMapper userMapper, SessionDataService sessionDataService) {
        this.userDataDAO = userDataDAO;
        this.studentDAO = studentDAO;
        this.supervisorDAO = supervisorDAO;
        this.studyYearDAO = studyYearDAO;
        this.userMapper = userMapper;
        this.sessionDataService = sessionDataService;
    }

    @Override
    public UserDTO getUser(String indexNumber, String studyYearFromHeader) {
        try {
            UserData userData = this.userDataDAO.findByIndexNumber(indexNumber).orElseThrow(()
                    -> new UserManagementException(MessageFormat.format("User with index: {0} not found", indexNumber)));

            final UserDTO userDTO = this.userMapper.mapToDto(userData);
            final String roleWithTheHighestPermissions = findRoleWithTheHighestPermissions(userData.getRoles());
            userDTO.setRole(roleWithTheHighestPermissions);

            final List<Long> acceptedProjects = new ArrayList<>();
            final List<Long> assignedProjects = new ArrayList<>();
            final List<String> studyYears = new ArrayList<>();

            if (hasRoleStudent(userData.getRoles())) {
                final List<Student> students = this.studentDAO.findAllByUserData_IndexNumber(indexNumber);

                final List<String> studentStudyYears = getStudyYearsForStudent(students);
                studyYears.addAll(studentStudyYears);

                String actualStudyYear = findActualStudyYear(studyYearFromHeader, indexNumber, studentStudyYears);

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
            } else if (hasRoleCoordinator(userData.getRoles())) {

                final List<String> coordinatorStudyYears = studyYearDAO.findAll().stream()
                        .map(StudyYear::getStudyYear)
                        .toList();
                studyYears.addAll(coordinatorStudyYears);

                String actualStudyYear = findActualStudyYear(studyYearFromHeader, indexNumber, coordinatorStudyYears);
                userDTO.setActualYear(actualStudyYear);

                if (hasRoleSupervisor(userData.getRoles())) {
                    final List<Supervisor> supervisors = this.supervisorDAO.findAllByUserData_IndexNumber(indexNumber);
                    final Supervisor entity = findSupervisorByActualStudyYear(supervisors, actualStudyYear, indexNumber);
                    if (Objects.nonNull(entity)) {
                        acceptedProjects.addAll(getSupervisorAcceptedProjects(entity));
                        assignedProjects.addAll(getSupervisorAssignedProjects(entity));
                    }
                }
            } else if (hasRoleSupervisor(userData.getRoles())) {
                final List<Supervisor> supervisors = this.supervisorDAO.findAllByUserData_IndexNumber(indexNumber);

                final List<String> supervisorStudyYears = getStudyYearsForSupervisor(supervisors);
                studyYears.addAll(supervisorStudyYears);

                String actualStudyYear = findActualStudyYear(studyYearFromHeader, indexNumber, supervisorStudyYears);

                userDTO.setActualYear(actualStudyYear);

                final Supervisor entity = findSupervisorByActualStudyYear(supervisors, actualStudyYear, indexNumber);
                if (Objects.isNull(entity)) {
                    throw new UserManagementException(
                            "Supervisor with index: " + indexNumber + " not found for study year: " + actualStudyYear);
                }
                acceptedProjects.addAll(getSupervisorAcceptedProjects(entity));
                assignedProjects.addAll(getSupervisorAssignedProjects(entity));
            }

            userDTO.setStudyYears(studyYears);
            userDTO.setAcceptedProjects(acceptedProjects);
            userDTO.setProjects(assignedProjects);
            log.info("User successfully fetched: {}", userDTO);
            return userDTO;

        } catch (Exception exception) {
            log.error("Exception during fetching the user data with index number: {} and study year: {}", indexNumber, studyYearFromHeader, exception);
            throw exception;
        }
    }

    private boolean hasRoleCoordinator(Set<Role> roles) {
        List<UserRole> roleNames = extractRoleNames(roles);
        return roleNames.contains(UserRole.COORDINATOR);
    }

    private String findActualStudyYear(String studyYear, String indexNumber, List<String> studyYears) {
        String actualStudyYearFromSessionData = sessionDataService.findActualStudyYear(indexNumber);
        String actualStudyYear;
        if (Objects.nonNull(actualStudyYearFromSessionData)) {
            actualStudyYear = actualStudyYearFromSessionData;
        } else {
            actualStudyYear = getActualStudyYear(studyYear, studyYears);
        }

        if (Objects.isNull(actualStudyYearFromSessionData)) {
            log.info("Actual study year was updated in SessionData");
            sessionDataService.updateActualStudyYear(actualStudyYear, indexNumber);
        }
        return actualStudyYear;
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
                .findFirst().orElse(null);
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
                .toList();
    }

}
