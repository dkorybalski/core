package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.enumerations.DefensePhase;

@Repository
public interface DefenseScheduleConfigDAO extends JpaRepository<DefenseScheduleConfig, Long> {

    DefenseScheduleConfig findByStudyYearAndDefensePhase(String studyYear, DefensePhase defensePhase);

    DefenseScheduleConfig findByStudyYearAndIsActiveIsTrue(String studyYear);
}
