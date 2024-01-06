package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;

import java.util.List;

@Repository
public interface DefenseTimeSlotDAO extends JpaRepository<DefenseTimeSlot, Long> {

    List<DefenseTimeSlot> findAllByDefenseScheduleConfig_Id(Long defenseScheduleConfigId);

    void deleteAllByDefenseScheduleConfig_Id(Long defenseScheduleCongifId);

}
