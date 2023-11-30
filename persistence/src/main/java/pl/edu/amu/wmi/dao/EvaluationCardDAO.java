package pl.edu.amu.wmi.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.amu.wmi.entity.EvaluationCard;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;

import java.util.List;

@Repository
public interface EvaluationCardDAO extends JpaRepository<EvaluationCard, Long> {

    List<EvaluationCard> findAllByEvaluationPhaseAndEvaluationStatusAndEvaluationCardTemplate_StudyYear(EvaluationPhase evaluationPhase, EvaluationStatus evaluationStatus, String studyYear);

}
