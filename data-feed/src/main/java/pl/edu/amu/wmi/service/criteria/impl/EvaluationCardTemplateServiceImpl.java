package pl.edu.amu.wmi.service.criteria.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateRepositoryDAO;
import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;
import pl.edu.amu.wmi.service.criteria.EvaluationCardTemplateService;

@Service
@Transactional
@Slf4j
public class EvaluationCardTemplateServiceImpl implements EvaluationCardTemplateService {

    private final EvaluationCardTemplateRepositoryDAO evaluationCardTemplateRepositoryDAO;

    public EvaluationCardTemplateServiceImpl(EvaluationCardTemplateRepositoryDAO evaluationCardTemplateRepositoryDAO) {
        this.evaluationCardTemplateRepositoryDAO = evaluationCardTemplateRepositoryDAO;
    }

    @Override
    public EvaluationCardTemplate updateEvaluationCardTemplate(String studyYear, EvaluationCriteriaDTO evaluationCriteriaDTO) {
        EvaluationCardTemplate evaluationCardTemplateForStudyYear =
                evaluationCardTemplateRepositoryDAO.findByStudyYear(studyYear).get();
        evaluationCardTemplateForStudyYear.setMinPointsThresholdFirstSemester(evaluationCriteriaDTO.minPointsThresholdFirstSemester());
        evaluationCardTemplateForStudyYear.setMinPointsThresholdSecondSemester(evaluationCriteriaDTO.minPointsThresholdSecondSemester());
        return evaluationCardTemplateRepositoryDAO.save(evaluationCardTemplateForStudyYear);
    }

    @Override
    public EvaluationCardTemplate saveEvaluationCardTemplate(String studyYear, EvaluationCriteriaDTO evaluationCriteriaDTO) {
        EvaluationCardTemplate evaluationCardTemplate = new EvaluationCardTemplate();
        evaluationCardTemplate.setStudyYear(studyYear);
        evaluationCardTemplate.setMinPointsThresholdFirstSemester(evaluationCriteriaDTO.minPointsThresholdFirstSemester());
        evaluationCardTemplate.setMinPointsThresholdSecondSemester(evaluationCriteriaDTO.minPointsThresholdSecondSemester());
        return evaluationCardTemplateRepositoryDAO.save(evaluationCardTemplate);
    }

    @Override
    public boolean existsByStudyYear(String studyYear) {
        return evaluationCardTemplateRepositoryDAO.existsByStudyYear(studyYear);
    }
}
