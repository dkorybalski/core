package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;

public interface DefenseScheduleConfigDAO extends JpaRepository<DefenseScheduleConfig, Long> {
}
