package pl.edu.amu.wmi.service.criteria.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateDAO;
import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;
import pl.edu.amu.wmi.service.criteria.EvaluationCardTemplateService;

@Service
@Transactional
@Slf4j
public class EvaluationCardTemplateServiceImpl implements EvaluationCardTemplateService {

    private final EvaluationCardTemplateDAO evaluationCardTemplateDAO;

    public EvaluationCardTemplateServiceImpl(EvaluationCardTemplateDAO evaluationCardTemplateDAO) {
        this.evaluationCardTemplateDAO = evaluationCardTemplateDAO;
    }

    @Override
    public EvaluationCardTemplate updateEvaluationCardTemplate(String studyYear, EvaluationCriteriaDTO evaluationCriteriaDTO) {
        EvaluationCardTemplate evaluationCardTemplateForStudyYear =
                evaluationCardTemplateDAO.findByStudyYear(studyYear).get();
        evaluationCardTemplateForStudyYear.setMinPointsThresholdFirstSemester(evaluationCriteriaDTO.minPointsThresholdFirstSemester());
        evaluationCardTemplateForStudyYear.setMinPointsThresholdSecondSemester(evaluationCriteriaDTO.minPointsThresholdSecondSemester());
        return evaluationCardTemplateDAO.save(evaluationCardTemplateForStudyYear);
    }

    @Override
    public EvaluationCardTemplate saveEvaluationCardTemplate(String studyYear, EvaluationCriteriaDTO evaluationCriteriaDTO) {
        EvaluationCardTemplate evaluationCardTemplate = new EvaluationCardTemplate();
        evaluationCardTemplate.setStudyYear(studyYear);
        evaluationCardTemplate.setMinPointsThresholdFirstSemester(evaluationCriteriaDTO.minPointsThresholdFirstSemester());
        evaluationCardTemplate.setMinPointsThresholdSecondSemester(evaluationCriteriaDTO.minPointsThresholdSecondSemester());
        return evaluationCardTemplateDAO.save(evaluationCardTemplate);
    }

    @Override
    public boolean existsByStudyYear(String studyYear) {
        return evaluationCardTemplateDAO.existsByStudyYear(studyYear);
    }
}
