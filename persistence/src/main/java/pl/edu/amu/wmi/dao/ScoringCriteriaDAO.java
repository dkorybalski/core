package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.ScoringCriteria;

@Repository
public interface ScoringCriteriaDAO extends JpaRepository<ScoringCriteria, Long> {
}
