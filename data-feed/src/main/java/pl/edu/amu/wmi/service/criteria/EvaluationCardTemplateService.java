package pl.edu.amu.wmi.service.criteria;

import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;

public interface EvaluationCardTemplateService {
    boolean existsByStudyYear(String studyYear);

    EvaluationCardTemplate saveEvaluationCardTemplate(String studyYear, EvaluationCriteriaDTO evaluationCriteriaDTO);

    EvaluationCardTemplate updateEvaluationCardTemplate(String studyYear, EvaluationCriteriaDTO evaluationCriteriaDTO);
}
