package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.EvaluationCardFreezeLog;

@Repository
public interface EvaluationCardFreezeLogDAO extends JpaRepository<EvaluationCardFreezeLog, Long> {
}
