package pl.edu.amu.wmi.service.grade;

import pl.edu.amu.wmi.entity.EvaluationCard;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.model.grade.EvaluationCardDetailsDTO;
import pl.edu.amu.wmi.model.grade.SingleGroupGradeUpdateDTO;
import pl.edu.amu.wmi.model.grade.UpdatedGradeDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface EvaluationCardService {

    void createEvaluationCard(Project project, String studyYear, Semester semester, EvaluationPhase phase, EvaluationStatus status, boolean isActive);

    UpdatedGradeDTO updateEvaluationCard(Long evaluationCardId, SingleGroupGradeUpdateDTO singleGroupGradeUpdate);

    Map<Semester, Map<EvaluationPhase, EvaluationCardDetailsDTO>> findEvaluationCards(Long projectId, String studyYear, String indexNumber);

    Optional<EvaluationCard> findTheMostRecentEvaluationCard(List<EvaluationCard> evaluationCards, Semester semester);

    String getPointsForSemester(Project entity, Semester semester);

    void publishEvaluationCard(Long projectId);

    void publishEvaluationCards(String studyYear);

    /**
     * Changes the status of evaluation card in semester phase to FROZEN and creates new card in defense phase, based on
     * a previous card in phase semester
     *
     * @param projectId        project id that the evaluation card is connected with
     */
    void freezeEvaluationCard(Long projectId);

    /**
     * Creates an evaluation card in phase retake
     *
     * @param projectId        project id that the evaluation card is connected with
     */
    void retakeEvaluationCard(Long projectId);

    void activateEvaluationCardsForSecondSemester(String studyYear);
}
