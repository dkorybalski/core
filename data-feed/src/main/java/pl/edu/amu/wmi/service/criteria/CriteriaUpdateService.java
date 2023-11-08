package pl.edu.amu.wmi.service.criteria;

import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.model.CriteriaSectionDTO;

public interface CriteriaUpdateService {
    void updateCriteriaSection(CriteriaSectionDTO criteriaSection, EvaluationCardTemplate updatedEvaluationCardTemplate);
}
