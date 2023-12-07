package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;

@Repository
public interface SupervisorDefenseAssignmentDAO extends JpaRepository<SupervisorDefenseAssignment, Long> {

    SupervisorDefenseAssignment findBySupervisor_IdAndDefenseTimeSlot_Id(Long supervisorId, Long defenseTimeSlotId);

}
