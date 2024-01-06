package pl.edu.amu.wmi.service.committee;

import java.time.LocalDate;
import java.util.List;

public interface SupervisorDefenseAssignmentService {

    void createSupervisorDefenseAssignments(String studyYear, Long defenseScheduleConfigId);

    List<LocalDate> getAllDefenseAssignmentDaysForStudyYear(String studyYear);

    void deleteAllConnectedWithDefenseScheduleConfig(Long defenseScheduleConfigId);
}
