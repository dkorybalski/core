package pl.edu.amu.wmi.service.supervisoravailability;

import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;

import java.util.Map;

public interface CommitteeService {

    void updateCommittee(String studyYear, Map<String, SupervisorDefenseAssignmentDTO> supervisorDefenseAssignmentDTOMap);
}
