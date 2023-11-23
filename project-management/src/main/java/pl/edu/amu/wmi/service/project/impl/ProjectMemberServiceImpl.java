package pl.edu.amu.wmi.service.project.impl;

import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.Role;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.service.project.ProjectMemberService;

import java.text.MessageFormat;

import static pl.edu.amu.wmi.enumerations.UserRole.*;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final UserDataDAO userDataDAO;

    public ProjectMemberServiceImpl(UserDataDAO userDataDAO) {
        this.userDataDAO = userDataDAO;
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

}
