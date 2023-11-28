package pl.edu.amu.wmi.service.project.impl;

import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.StudentDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.service.project.ProjectMemberService;

import java.text.MessageFormat;
import java.util.Objects;

import static pl.edu.amu.wmi.enumerations.UserRole.*;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final UserDataDAO userDataDAO;
    private final StudentDAO studentDAO;

    public ProjectMemberServiceImpl(UserDataDAO userDataDAO, StudentDAO studentDAO) {
        this.userDataDAO = userDataDAO;
        this.studentDAO = studentDAO;
    }

    @Override
    public UserRole getUserRoleByUserIndex(String index) {
        UserData userData = userDataDAO.findByIndexNumber(index).orElseThrow(()
                -> new BusinessException(MessageFormat.format("User with index: {0} not found.", index)));
        Role userRole = userData.getRoles().stream()
                .filter(role -> role.getName().equals(STUDENT) || role.getName().equals(SUPERVISOR))
                .findFirst().orElseThrow(()
                        -> new BusinessException(MessageFormat.format("User with index: {0} does not have required role.", index)));
        return userRole.getName();
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

}
