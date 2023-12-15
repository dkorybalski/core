package pl.edu.amu.wmi.dao;

import jakarta.persistence.Tuple;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.model.CommitteeAssignmentCriteria;

import java.util.List;

public interface CommitteeMemberDAO {

    List<Tuple> findCommitteeChairpersonsPerDayAndPerStudyYear(String studyYear);

    List<SupervisorDefenseAssignment> findAllAssignmentsByCriteria(CommitteeAssignmentCriteria criteria);
}
