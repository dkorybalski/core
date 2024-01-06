package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;

import java.util.List;

@Repository
public interface SupervisorDefenseAssignmentDAO extends JpaRepository<SupervisorDefenseAssignment, Long> {

    @EntityGraph(attributePaths = {"projectDefense"})
    SupervisorDefenseAssignment findBySupervisor_IdAndDefenseTimeSlot_Id(Long supervisorId, Long defenseTimeSlotId);

    List<SupervisorDefenseAssignment> findAllBySupervisor_Id(Long supervisorId);

    List<SupervisorDefenseAssignment> findAllByDefenseTimeSlot_StudyYearAndDefenseTimeSlot_DefenseScheduleConfig_IsActiveIsTrue(String studyYear);

    @EntityGraph(attributePaths = {"projectDefense"})
    List<SupervisorDefenseAssignment> findAllByDefenseTimeSlot_DefenseScheduleConfig_Id(Long id);

}
