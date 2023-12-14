package pl.edu.amu.wmi.service.committee;

import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;

import java.util.Map;


public interface SupervisorAvailabilityService {

    void putSupervisorAvailability(String studyYear, Long supervisorId, Map<String, SupervisorDefenseAssignmentDTO> supervisorDefenseAssignments);

    Map<String, Map<String, SupervisorDefenseAssignmentDTO>> getSupervisorAvailabilitySurvey(Long supervisorId);

    Map<String, Map<String, Map<String, SupervisorDefenseAssignmentDTO>>> getAggregatedSupervisorsAvailability(String studyYear);

}
