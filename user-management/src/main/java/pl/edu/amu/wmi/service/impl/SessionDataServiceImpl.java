package pl.edu.amu.wmi.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.SessionDataDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.SessionData;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.exception.UserManagementException;
import pl.edu.amu.wmi.service.SessionDataService;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

@Service
public class SessionDataServiceImpl implements SessionDataService {

    private final SessionDataDAO sessionDataDAO;

    private final UserDataDAO userDataDAO;

    public SessionDataServiceImpl(SessionDataDAO sessionDataDAO, UserDataDAO userDataDAO) {
        this.sessionDataDAO = sessionDataDAO;
        this.userDataDAO = userDataDAO;
    }

    @Override
    @Transactional
    public void updateActualStudyYear(String updatedStudyYear, String indexNumber) {
        Optional<SessionData> persistedSessionData = sessionDataDAO.findByUserData_IndexNumber(indexNumber);
        SessionData entity;
        if (persistedSessionData.isEmpty()) {
            UserData userData = this.userDataDAO.findByIndexNumber(indexNumber).orElseThrow(()
                    -> new UserManagementException(MessageFormat.format("User with index: {0} not found", indexNumber)));
            entity = new SessionData();
            entity.setUserData(userData);
        } else {
            entity = persistedSessionData.get();
        }
        entity.setActualStudyYear(updatedStudyYear);
        sessionDataDAO.save(entity);
    }

    @Override
    public String findActualStudyYear(String indexNumber) {
        SessionData sessionData = sessionDataDAO.findByUserData_IndexNumber(indexNumber).orElse(null);
        return Objects.isNull(sessionData) ? null : sessionData.getActualStudyYear();
    }
}
