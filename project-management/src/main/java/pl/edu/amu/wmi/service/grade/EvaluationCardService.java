package pl.edu.amu.wmi.service.grade;

import pl.edu.amu.wmi.entity.EvaluationCard;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.model.grade.EvaluationCardDetails;
import pl.edu.amu.wmi.model.grade.SingleGroupGradeUpdateDTO;
import pl.edu.amu.wmi.model.grade.UpdatedGradeDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface EvaluationCardService {

    void createEvaluationCard(Project project, String studyYear, Semester semester, EvaluationPhase phase, EvaluationStatus status);

    UpdatedGradeDTO updateEvaluationCard(Long evaluationCardId, SingleGroupGradeUpdateDTO singleGroupGradeUpdate);

    Map<Semester, Map<EvaluationPhase, EvaluationCardDetails>> findEvaluationCards(Long projectId, String studyYear, String indexNumber);

    Optional<EvaluationCard> findTheMostRecentEvaluationCard(List<EvaluationCard> evaluationCards, Semester semester);

    String getPointsForSemester(Project entity, Semester semester);

}
