package pl.edu.amu.wmi.service.project.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.mapper.project.SupervisorProjectMapper;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pl.edu.amu.wmi.utils.Constants.STUDY_YEAR_FULL_TIME_2023;

@ExtendWith(MockitoExtension.class)
class SupervisorProjectServiceImplTest {

    @Mock
    private SupervisorDAO supervisorDAO;

    @Mock
    private SupervisorProjectMapper supervisorProjectMapper;

    @InjectMocks
    private SupervisorProjectServiceImpl supervisorProjectService;

    @Test
    void isSupervisorAvailable_successful() {
        //given
        String supervisorIndexNumber = "SUPERVISOR_1";
        Supervisor supervisor = createSupervisor(supervisorIndexNumber);
        Mockito.when(supervisorDAO.findByStudyYearAndUserData_IndexNumber(STUDY_YEAR_FULL_TIME_2023, supervisorIndexNumber)).thenReturn(supervisor);
        //when
        boolean isAvailable = this.supervisorProjectService.isSupervisorAvailable(STUDY_YEAR_FULL_TIME_2023, supervisorIndexNumber);
        //then
        assertTrue(isAvailable);

        verify(supervisorDAO, times(1)).findByStudyYearAndUserData_IndexNumber(STUDY_YEAR_FULL_TIME_2023, supervisorIndexNumber);
    }


    @Test
    void isSupervisorAvailable_SupervisorNotFound() {
        //given
        String supervisorIndexNumber = "NOT_EXISTING_SUPERVISOR";
        Mockito.when(supervisorDAO.findByStudyYearAndUserData_IndexNumber(STUDY_YEAR_FULL_TIME_2023, supervisorIndexNumber)).thenReturn(null);
        //when then
        assertThrows(BusinessException.class, () -> this.supervisorProjectService.isSupervisorAvailable(STUDY_YEAR_FULL_TIME_2023, supervisorIndexNumber));
        verify(supervisorDAO, times(1)).findByStudyYearAndUserData_IndexNumber(STUDY_YEAR_FULL_TIME_2023, supervisorIndexNumber);
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
}
