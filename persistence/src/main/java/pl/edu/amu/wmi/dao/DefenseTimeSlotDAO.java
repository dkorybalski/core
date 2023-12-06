package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;

import java.util.List;

public interface DefenseTimeSlotDAO extends JpaRepository<DefenseTimeSlot, Long> {

    List<DefenseTimeSlot> findAllByDefenseScheduleConfig_Id(Long defenseScheduleConfigId);

}
