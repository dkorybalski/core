package pl.edu.amu.wmi.service.committee;

import pl.edu.amu.wmi.model.committee.SupervisorStatisticsDTO;

import java.util.List;

public interface SupervisorStatisticsService {
    List<SupervisorStatisticsDTO> getSupervisorStatistics(String studyYear);
}
