package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.EvaluationCardDAO;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.exception.ProjectManagementException;
import pl.edu.amu.wmi.service.EvaluationCardService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class EvaluationCardServiceImpl implements EvaluationCardService {

    private final EvaluationCardDAO evaluationCardDAO;
    private final EvaluationCardTemplateDAO evaluationCardTemplateDAO;

    public EvaluationCardServiceImpl(EvaluationCardDAO evaluationCardDAO,
                                     EvaluationCardTemplateDAO evaluationCardTemplateDAO) {
        this.evaluationCardDAO = evaluationCardDAO;
        this.evaluationCardTemplateDAO = evaluationCardTemplateDAO;
    }

    @Override
    @Transactional
    public void addEmptyGradesToEvaluationCard(Project project, String studyYear) {
        Optional<EvaluationCardTemplate> evaluationCardTemplate = evaluationCardTemplateDAO.findByStudyYear(studyYear);
        if (evaluationCardTemplate.isEmpty()) {
            log.info("Evaluation criteria have been not yet uploaded to the system - EvaluationCard will be updated later");
            return;
        }
        EvaluationCardTemplate template = evaluationCardTemplate.get();

        List<Grade> grades = createEmptyGrades(template);

        EvaluationCard evaluationCard = evaluationCardDAO.findById(project.getId()).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Evaluation card for project: {0} not found", project.getId())));
        evaluationCard.setEvaluationCardTemplate(template);
        evaluationCard.setGrades(grades);
        evaluationCardDAO.save(evaluationCard);
    }

    private List<Grade> createEmptyGrades(EvaluationCardTemplate template) {
        List<Grade> grades = new ArrayList<>();
        grades.addAll(createEmptyGradesForSemester(template.getCriteriaSectionsFirstSemester()));
        grades.addAll(createEmptyGradesForSemester(template.getCriteriaSectionsSecondSemester()));
        return grades;
    }

    private List<Grade> createEmptyGradesForSemester(List<CriteriaSection> criteriaSections) {
        List<Grade> grades = new ArrayList<>();
        criteriaSections.forEach(criteriaSection -> {
                    List<Grade> gradesForSection = createEmptyGradesForCriteriaSection(criteriaSection);
                    grades.addAll(gradesForSection);
                });
        return grades;
    }

    private List<Grade> createEmptyGradesForCriteriaSection(CriteriaSection criteriaSection) {
        return criteriaSection.getCriteriaGroups().stream()
                .map(Grade::new)
                .toList();
    }
}
