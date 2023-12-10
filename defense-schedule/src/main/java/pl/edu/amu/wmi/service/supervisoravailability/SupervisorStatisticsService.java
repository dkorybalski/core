package pl.edu.amu.wmi.service.supervisoravailability;

import pl.edu.amu.wmi.model.supervisordefense.SupervisorStatisticsDTO;

import java.util.List;

public interface SupervisorStatisticsService {
    List<SupervisorStatisticsDTO> getSupervisorStatistics(String studyYear);
}
