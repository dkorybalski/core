package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;

public interface DefenseTimeSlotDAO extends JpaRepository<DefenseTimeSlot, Long> {
}
