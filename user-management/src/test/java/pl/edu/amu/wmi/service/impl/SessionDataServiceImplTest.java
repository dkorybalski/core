package pl.edu.amu.wmi.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.amu.wmi.dao.SessionDataDAO;
import pl.edu.amu.wmi.dao.UserDataDAO;
import pl.edu.amu.wmi.entity.SessionData;
import pl.edu.amu.wmi.entity.UserData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class SessionDataServiceImplTest {

    @Mock
    private SessionDataDAO sessionDataDAO;

    @Mock
    private UserDataDAO userDataDAO;

    @InjectMocks
    private SessionDataServiceImpl sessionDataService;

    @Test
    void updateActualStudyYear_sessionDataObjectAlreadyExists_successful() {
        //given
        String indexNumber = "s123456";
        String currentStudyYear = "FULL_TIME#2023";
        String updatedStudyYear = "PART_TIME#2024";
        SessionData sessionData = new SessionData();
        sessionData.setActualStudyYear(currentStudyYear);
        Mockito.when(sessionDataDAO.findByUserData_IndexNumber(indexNumber)).thenReturn(Optional.of(sessionData));
        //when
        sessionDataService.updateActualStudyYear(updatedStudyYear, indexNumber);
        //then
        ArgumentCaptor<SessionData> captor = ArgumentCaptor.forClass(SessionData.class);
        Mockito.verify(sessionDataDAO, times(1)).findByUserData_IndexNumber(indexNumber);
        Mockito.verify(userDataDAO, times(0)).findByIndexNumber(indexNumber);
        Mockito.verify(sessionDataDAO, times(1)).save(captor.capture());

        SessionData updatedSessionData = captor.getValue();
        assertThat(updatedSessionData.getActualStudyYear()).isEqualTo(updatedStudyYear);
    }

    @Test
    void updateActualStudyYear_sessionDataObjectDoesNotExist_successful() {
        //given
        String indexNumber = "s123456";
        String updatedStudyYear = "PART_TIME#2024";
        UserData userData = new UserData();
        userData.setIndexNumber(indexNumber);

        Mockito.when(sessionDataDAO.findByUserData_IndexNumber(indexNumber)).thenReturn(Optional.empty());
        Mockito.when(userDataDAO.findByIndexNumber(indexNumber)).thenReturn(Optional.of(userData));
        //when
        sessionDataService.updateActualStudyYear(updatedStudyYear, indexNumber);
        //then
        ArgumentCaptor<SessionData> captor = ArgumentCaptor.forClass(SessionData.class);
        Mockito.verify(sessionDataDAO, times(1)).findByUserData_IndexNumber(indexNumber);
        Mockito.verify(userDataDAO, times(1)).findByIndexNumber(indexNumber);
        Mockito.verify(sessionDataDAO, times(1)).save(captor.capture());

        SessionData updatedSessionData = captor.getValue();
        assertThat(updatedSessionData.getActualStudyYear()).isEqualTo(updatedStudyYear);
        assertThat(updatedSessionData.getUserData().getIndexNumber()).isEqualTo(indexNumber);
    }

    @Test
    void findActualStudyYear_studyYearNotNull_successful() {
        //given
        String indexNumber = "s123456";
        String actualStudyYear = "FULL_TIME#2023";
        SessionData sessionData = new SessionData();
        sessionData.setActualStudyYear(actualStudyYear);
        Mockito.when(sessionDataDAO.findByUserData_IndexNumber(indexNumber)).thenReturn(Optional.of(sessionData));
        //when
        String resultStudyYear = sessionDataService.findActualStudyYear(indexNumber);
        //then
        Mockito.verify(sessionDataDAO, times(1)).findByUserData_IndexNumber(indexNumber);

        assertThat(resultStudyYear).isEqualTo(actualStudyYear);
    }

}
