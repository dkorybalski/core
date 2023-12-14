package pl.edu.amu.wmi.service.committee;

import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.model.committee.ChairpersonAssignmentDTO;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;

import java.util.Map;

public interface CommitteeService {

    void updateCommittee(String studyYear, Map<String, SupervisorDefenseAssignmentDTO> supervisorDefenseAssignmentDTOMap);

    Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> getAggregatedChairpersonAssignments(String studyYear);
}
