package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;

public interface SupervisorDefenseAssignmentDAO extends JpaRepository<SupervisorDefenseAssignment, Long> {
}
