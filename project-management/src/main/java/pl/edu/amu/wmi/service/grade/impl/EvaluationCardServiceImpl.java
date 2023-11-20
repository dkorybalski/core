package pl.edu.amu.wmi.service.grade.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.EvaluationCardDAO;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.exception.grade.EvaluationCardException;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.model.grade.GradeDetailsDTO;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;
import pl.edu.amu.wmi.service.grade.GradeService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class EvaluationCardServiceImpl implements EvaluationCardService {

    private final EvaluationCardDAO evaluationCardDAO;
    private final EvaluationCardTemplateDAO evaluationCardTemplateDAO;
    private final GradeService gradeService;

    public EvaluationCardServiceImpl(EvaluationCardDAO evaluationCardDAO,
                                     EvaluationCardTemplateDAO evaluationCardTemplateDAO,
                                     GradeService gradeService) {
        this.evaluationCardDAO = evaluationCardDAO;
        this.evaluationCardTemplateDAO = evaluationCardTemplateDAO;
        this.gradeService = gradeService;
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


    // TODO: 11/18/2023 - Once SYSPRI-223 is ready, update the disqualification and approval conditions
    /**
     * Updates project evaluation card based on received GradeDetailsDTO object. Method triggers
     * GradeService.updateProjectGradesForSemester(Semester semester, List<Grade> projectGradesForSemester, GradeDetailsDTO projectGradeDetails)
     * method to update single grades entities belonging to the project evaluation card.
     * Based on updated grades points and evaluation card qualification is calculated.
     * Also, each Criteria Group modification date is being updated (TODO 11/19.2023: SYSPRI-231)
     * On the end method returns updated project's grades information as a GradeDetailsDTO object.
     *
     * @param semester  - semester that the grades are updated for
     * @param projectId - project that the grades update is for
     * @param gradeDetails - request body containing all update information
     * @return GradeDetailsDTO - contain updated project's grades information
     */
    @Override
    @Transactional
    public GradeDetailsDTO updateEvaluationCard(Semester semester, Long projectId, GradeDetailsDTO gradeDetails) {
        EvaluationCard evaluationCard = evaluationCardDAO.findById(projectId)
                .orElseThrow(() -> new EvaluationCardException(MessageFormat.format("Evaluation card for project with id: {0} not found", projectId)));

        List<Grade> gradesForSemester = getGradesForSemester(semester, evaluationCard);
        gradeService.updateProjectGradesForSemester(semester, gradesForSemester, gradeDetails);

        updateCriteriaGroupModificationDate();

        Double totalPointsSemester = calculateTotalPointsWithWeight(gradesForSemester);
        switch (semester) {
            case SEMESTER_I ->
                    evaluationCard.setTotalPointsFirstSemester(totalPointsSemester);
            case SEMESTER_II ->
                    evaluationCard.setTotalPointsSecondSemester(totalPointsSemester);
        }

        boolean isDisqualified = checkDisqualification(gradesForSemester);
        evaluationCard.setDisqualified(isDisqualified);
        evaluationCard.setApprovedForDefense(!isDisqualified);
        evaluationCardDAO.save(evaluationCard);

        return gradeService.findByProjectIdAndSemester(semester, projectId);
    }

    /**
     * Fetches grades for semester from provided evaluation card.
     */
    private List<Grade> getGradesForSemester(Semester semester, EvaluationCard evaluationCard) {
        List<Grade> grades = evaluationCard.getGrades();
        return grades.stream()
                .filter(grade -> isGradeForSemester(grade, semester))
                .toList();
    }

    /**
     * Confirms if grade belongs to the semester.
     */
    private boolean isGradeForSemester(Grade grade, Semester semester) {
        return grade.getCriteriaGroup().getCriteriaSection().getSemester().equals(semester);
    }

    private void updateCriteriaGroupModificationDate() {
        // TODO 11/19.2023: SYSPRI-231
    }

    /**
     * Calculates total points with weight (weighted average) of provided grade objects.
     * Every grade belongs to the criteria group which contains grade weight.
     * Then the overall weight is used as a divisor for a sum of weighted points.
     */
    private Double calculateTotalPointsWithWeight(List<Grade> grades) {
        List<Double> gradesWeightsForSemester = grades.stream()
                .map(grade -> grade.getCriteriaGroup().getGradeWeight()).toList();

        Double totalPointsWithWeight = grades.stream()
                .map(Grade::getPointsWithWeight)
                .filter(Objects::nonNull)
                .reduce(0.0, Double::sum);
        Double totalWeight = gradesWeightsForSemester.stream()
                .filter(Objects::nonNull)
                .reduce(0.0, Double::sum);

        return totalPointsWithWeight / totalWeight;
    }

    /**
     * Confirms if all grades are selected and none of them are disqualifying.
     */
    private boolean checkDisqualification(List<Grade> gradesForSemester) {
        return gradesForSemester.stream().anyMatch(Grade::isDisqualifying) ||
                gradesForSemester.stream().anyMatch(g -> g.getPointsWithWeight() == null);
    }

}
