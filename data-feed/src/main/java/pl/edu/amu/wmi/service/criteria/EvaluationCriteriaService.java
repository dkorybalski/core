package pl.edu.amu.wmi.service.criteria;

import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.entity.CriteriaSection;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;
import pl.edu.amu.wmi.model.CriteriaSectionDTO;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;

public interface EvaluationCriteriaService {

    /**
     * Creates object of type {@link EvaluationCriteriaDTO} which contain all evaluation criteria connected with a study year.
     * If criteria with the same name exist for both semesters (objects: {@link CriteriaSection} or {@link CriteriaGroup}),
     * then one dto object is created for them (respectively {@link CriteriaSectionDTO} or {@link CriteriaGroupDTO})
     *
     * @param studyYear - study year that the criteria are fetched for
     * @return evaluation criteria for a study year. If evaluation criteria does not exist for a study year, an empty object is returned
     */
    EvaluationCriteriaDTO constructEvaluationCriteriaDTO(String studyYear);
}
