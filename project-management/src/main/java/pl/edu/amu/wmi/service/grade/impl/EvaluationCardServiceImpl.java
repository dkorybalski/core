package pl.edu.amu.wmi.service.grade.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.EvaluationCardDAO;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateDAO;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.*;
import pl.edu.amu.wmi.exception.grade.EvaluationCardException;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.mapper.grade.ProjectCriteriaSectionMapper;
import pl.edu.amu.wmi.model.grade.CriteriaSectionDTO;
import pl.edu.amu.wmi.model.grade.EvaluationCardDetails;
import pl.edu.amu.wmi.model.grade.SingleGroupGradeUpdateDTO;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;
import pl.edu.amu.wmi.service.grade.GradeService;
import pl.edu.amu.wmi.service.project.ProjectMemberService;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
@Service
public class EvaluationCardServiceImpl implements EvaluationCardService {

    private final EvaluationCardDAO evaluationCardDAO;
    private final EvaluationCardTemplateDAO evaluationCardTemplateDAO;
    private final ProjectDAO projectDAO;
    private final GradeService gradeService;
    private final ProjectMemberService projectMemberService;
    private final ProjectCriteriaSectionMapper projectCriteriaSectionMapper;

    public EvaluationCardServiceImpl(EvaluationCardDAO evaluationCardDAO,
                                     EvaluationCardTemplateDAO evaluationCardTemplateDAO,
                                     ProjectDAO projectDAO,
                                     GradeService gradeService,
                                     ProjectMemberService projectMemberService, ProjectCriteriaSectionMapper projectCriteriaSectionMapper) {
        this.evaluationCardDAO = evaluationCardDAO;
        this.evaluationCardTemplateDAO = evaluationCardTemplateDAO;
        this.projectDAO = projectDAO;
        this.gradeService = gradeService;
        this.projectMemberService = projectMemberService;
        this.projectCriteriaSectionMapper = projectCriteriaSectionMapper;
    }

    @Override
    @Transactional
    public void createEvaluationCard(Project project, String studyYear, Semester semester, EvaluationPhase phase, EvaluationStatus status) {
        Optional<EvaluationCardTemplate> evaluationCardTemplate = evaluationCardTemplateDAO.findByStudyYear(studyYear);
        if (evaluationCardTemplate.isEmpty()) {
            log.info("Evaluation criteria have been not yet uploaded to the system - EvaluationCard will be updated later");
            return;
        }
        EvaluationCardTemplate template = evaluationCardTemplate.get();
        List<Grade> grades = createEmptyGrades(template);

        EvaluationCard evaluationCard = new EvaluationCard();
        evaluationCard.setEvaluationCardTemplate(template);
        evaluationCard.setGrades(grades);

        evaluationCard.setSemester(semester);
        evaluationCard.setEvaluationPhase(phase);
        evaluationCard.setEvaluationStatus(status);

        project.addEvaluationCard(evaluationCard);

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

    /**
     * Creates an EvaluationCardDTO object that contains grades information for a specific project.
     * In details, EvaluationCardDTO object contains all evaluation cards assigned to a project, grouped by semesters
     * and evaluation phases
     * In addition to information about the criteria related to the evaluation card, there is also information about the
     * selected criteria for each Criteria Group for every existing evaluation card.
     * The selected criterion is calculated based on the points obtained by
     * the evaluation card in a specific Criteria Group.
     *
     * @param projectId - project that the evaluation cards are fetched for
     * @param studyYear - study year that the evaluation cards are fetched for
     * @return object which contains information about all evaluation cards assigned to the project grouped by semesters
     * and evaluation phases
     */
    @Override
    public Map<Semester, Map<EvaluationPhase, EvaluationCardDetails>> findEvaluationCards(Long projectId, String studyYear, String indexNumber) {

        Project project = projectDAO.findById(projectId)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));


        EvaluationCardTemplate evaluationCardTemplate = evaluationCardTemplateDAO.findByStudyYear(studyYear)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Evaluation card template for project with id: {0} not found", projectId)));

        List<EvaluationCard> evaluationCardsEntities = project.getEvaluationCards();

        Map<EvaluationPhase, EvaluationCardDetails> evaluationCardsFirstSemester = new HashMap<>();
        Map<EvaluationPhase, EvaluationCardDetails> evaluationCardsSecondSemester = new HashMap<>();

        evaluationCardsEntities.forEach(evaluationCardEntity -> {
            EvaluationCardDetails evaluationCardDetails = createEvaluationCardDetails(evaluationCardEntity, project, evaluationCardTemplate, indexNumber);

            if (Objects.equals(Semester.FIRST, evaluationCardEntity.getSemester())) {
                evaluationCardsFirstSemester.put(evaluationCardEntity.getEvaluationPhase(), evaluationCardDetails);
            } else {
                evaluationCardsSecondSemester.put(evaluationCardEntity.getEvaluationPhase(), evaluationCardDetails);
            }
        });

        Map<Semester, Map<EvaluationPhase, EvaluationCardDetails>> evaluationCardMap = new HashMap<>();
        evaluationCardMap.put(Semester.FIRST, evaluationCardsFirstSemester);
        evaluationCardMap.put(Semester.SECOND, evaluationCardsSecondSemester);

        return evaluationCardMap;
    }

    private EvaluationCardDetails createEvaluationCardDetails(EvaluationCard evaluationCardEntity, Project project, EvaluationCardTemplate evaluationCardTemplate, String indexNumber) {
        EvaluationCardDetails evaluationCardDetails = new EvaluationCardDetails();
        evaluationCardDetails.setId(evaluationCardEntity.getId());
        evaluationCardDetails.setGrade(pointsToOverallPercent(evaluationCardEntity.getTotalPoints()));

        if (!Objects.equals(AcceptanceStatus.ACCEPTED, project.getAcceptanceStatus())) {
            // TODO: 11/23/2023 editable to false
        }

        boolean isEditable = determineIfEvaluationCardIsEditable(evaluationCardEntity, project, indexNumber);

        evaluationCardDetails.setEditable(isEditable);
        evaluationCardDetails.setVisible(true);

        List<CriteriaSection> sections = getCriteriaSectionsForSemester(evaluationCardTemplate, evaluationCardEntity.getSemester());
        List<CriteriaSectionDTO> sectionDTOs = projectCriteriaSectionMapper.mapToDtoList(sections);

        List<Grade> projectGrades = evaluationCardEntity.getGrades();
        Map<Long, Integer> projectPointsByGroupId = new HashMap<>();
        for (Grade projectGrade : projectGrades) {
            projectPointsByGroupId.put(projectGrade.getCriteriaGroup().getId(), projectGrade.getPoints());
        }

        sectionDTOs.forEach(section -> section.getCriteriaGroups().forEach(group ->
                group.setSelectedCriterion(CriterionCategory.getByPointsReceived(projectPointsByGroupId.get(group.getId())))
        ));

        evaluationCardDetails.setSections(sectionDTOs);
        return evaluationCardDetails;
    }

    private boolean isUserAProjectSupervisor(Supervisor supervisor, String indexNumber) {
        return Objects.equals(supervisor.getIndexNumber(), indexNumber);
    }

    private boolean determineIfEvaluationCardIsEditable(EvaluationCard evaluationCardEntity, Project project, String indexNumber) {
        if (isUserAProjectSupervisor(project.getSupervisor(), indexNumber) && Objects.equals(EvaluationStatus.ACTIVE, evaluationCardEntity.getEvaluationStatus())) {
            return true;
        }
        if (Objects.equals(UserRole.SUPERVISOR, projectMemberService.getUserRoleByUserIndex(indexNumber))
                && !Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCardEntity.getEvaluationPhase())
                && Objects.equals(EvaluationStatus.ACTIVE, evaluationCardEntity.getEvaluationStatus())) {
            return true;
        }
        if (projectMemberService.isUserRoleCoordinator(indexNumber)) {
            return true;
        }
        return false;
    }

    /**
     * Calculates points based on data stored in EvaluationCard entity which are in range 0.0 - 4.0.
     * The method goal is to return string representation of the value as a percent.
     * To do so it use operation of proportion. As value 4 is 100% then it is divisor.
     * If evaluation card doesn't have points, then null value is the method input, then 0.0% is returned.
     */
    private String pointsToOverallPercent(Double points) {
        if (Objects.isNull(points))
            return "0.0%";
        Double pointsOverall = points * 100 / 4;
        return String.format("%.2f", pointsOverall) + "%";
    }

    /**
     * Returns list of criteria sections which are related to the project in chosen semester based on evaluation card template
     */
    private List<CriteriaSection> getCriteriaSectionsForSemester(EvaluationCardTemplate template, Semester semester) {
        return switch (semester) {
            case FIRST ->
                    template.getCriteriaSectionsFirstSemester();
            case SECOND ->
                    template.getCriteriaSectionsSecondSemester();
        };
    }



    // TODO: 11/18/2023 - Once SYSPRI-223 is ready, update the disqualification and approval conditions
    @Override
    @Transactional
    public SingleGroupGradeUpdateDTO updateEvaluationCard(Long evaluationCardId, SingleGroupGradeUpdateDTO singleGroupGradeUpdate) {
        EvaluationCard evaluationCard = evaluationCardDAO.findById(evaluationCardId)
                .orElseThrow(() -> new EvaluationCardException(MessageFormat.format("Evaluation card with id: {0} not found", evaluationCardId)));

        // TODO 11/22/2023: When Evaluation Card changes are completed, then semester needs to be taken from Evaluation Card.
        Semester semester = Semester.FIRST;
//        Semester semester = evaluationCard.getSemester();

        Long criteriaGroupId = singleGroupGradeUpdate.getId();
        Grade gradeToUpdate = evaluationCard.getGrades()
                .stream()
                .filter(g -> g.getCriteriaGroup().getId().equals(criteriaGroupId))
                .findFirst()
                .orElseThrow(() -> new EvaluationCardException(MessageFormat.format("Grade for Criteria Group with id: {0} not found", criteriaGroupId)));

        CriterionCategory newSelectedCriterion = singleGroupGradeUpdate.getSelectedCriterion();
        gradeService.updateSingleGrade(gradeToUpdate, newSelectedCriterion);

        List<Grade> gradesForSemester = getGradesForSemester(semester, evaluationCard);

        updateCriteriaGroupModificationDate();

        Double totalPointsSemester = calculateTotalPointsWithWeight(gradesForSemester);
        evaluationCard.setTotalPoints(totalPointsSemester);

        // TODO 11/22/2023: SYSPRI-223 - when the task is completed, disqualification logic must be changed.
        boolean isDisqualified = checkDisqualification(gradesForSemester);
        evaluationCard.setDisqualified(isDisqualified);
        // TODO 11/22/2023: When evaluation card changes are completed, approval logic must be changed.
        evaluationCard.setApprovedForDefense(!isDisqualified);
        evaluationCardDAO.save(evaluationCard);

        return new SingleGroupGradeUpdateDTO(singleGroupGradeUpdate.getId(), CriterionCategory.getByPointsReceived(gradeToUpdate.getPoints()));
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
