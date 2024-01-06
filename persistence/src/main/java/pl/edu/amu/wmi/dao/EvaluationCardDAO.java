package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.EvaluationCard;

import java.util.List;

@Repository
public interface EvaluationCardDAO extends JpaRepository<EvaluationCard, Long> {

    List<EvaluationCard> findAllByProject_Id(Long projectId);
}
