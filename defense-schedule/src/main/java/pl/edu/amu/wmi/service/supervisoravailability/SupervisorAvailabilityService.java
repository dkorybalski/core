package pl.edu.amu.wmi.service.supervisoravailability;

import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;

import java.util.List;
import java.util.Map;


public interface SupervisorAvailabilityService {

    void putSupervisorAvailability(String studyYear, Long supervisorId, SupervisorDefenseAssignmentDTO supervisorDefenseAssignment);

    Map<String, List<SupervisorDefenseAssignmentDTO>> getSupervisorAvailabilitySurvey(Long supervisorId);
}
