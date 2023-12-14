package pl.edu.amu.wmi.service.committee;

import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;

import java.util.Map;

public interface CommitteeService {

    void updateCommittee(String studyYear, Map<String, SupervisorDefenseAssignmentDTO> supervisorDefenseAssignmentDTOMap);
}
