package pl.edu.amu.wmi.service.supervisoravailability;

import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;


public interface SupervisorAvailabilityService {

    void putSupervisorAvailability(String studyYear, Long supervisorId, SupervisorDefenseAssignmentDTO supervisorDefenseAssignment);

}
