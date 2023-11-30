package pl.edu.amu.wmi.service.grade.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.EvaluationCardDAO;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateDAO;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.CriterionCategory;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.exception.grade.EvaluationCardException;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.mapper.grade.ProjectCriteriaSectionMapper;
import pl.edu.amu.wmi.model.grade.*;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;
import pl.edu.amu.wmi.service.grade.GradeService;
import pl.edu.amu.wmi.service.permission.PermissionService;
import pl.edu.amu.wmi.service.project.ProjectMemberService;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
@Service
public class EvaluationCardServiceImpl implements EvaluationCardService {

    private final EvaluationCardDAO evaluationCardDAO;
    private final EvaluationCardTemplateDAO evaluationCardTemplateDAO;
    private final ProjectDAO projectDAO;
    private final GradeService gradeService;
    private final PermissionService permissionService;
    private final ProjectMemberService projectMemberService;
    private final ProjectCriteriaSectionMapper projectCriteriaSectionMapper;

    public EvaluationCardServiceImpl(EvaluationCardDAO evaluationCardDAO,
                                     EvaluationCardTemplateDAO evaluationCardTemplateDAO,
                                     ProjectDAO projectDAO,
                                     GradeService gradeService,
                                     PermissionService permissionService,
                                     ProjectMemberService projectMemberService,
                                     ProjectCriteriaSectionMapper projectCriteriaSectionMapper) {
        this.evaluationCardDAO = evaluationCardDAO;
        this.evaluationCardTemplateDAO = evaluationCardTemplateDAO;
        this.projectDAO = projectDAO;
        this.gradeService = gradeService;
        this.permissionService = permissionService;
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
        evaluationCard.setTotalPoints(0.0);

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
    public Map<Semester, Map<EvaluationPhase, EvaluationCardDetailsDTO>> findEvaluationCards(Long projectId, String studyYear, String indexNumber) {

        Project project = projectDAO.findById(projectId)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));


        EvaluationCardTemplate evaluationCardTemplate = evaluationCardTemplateDAO.findByStudyYear(studyYear)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Evaluation card template for project with id: {0} not found", projectId)));

        List<EvaluationCard> evaluationCardsEntities = project.getEvaluationCards();

        Map<EvaluationPhase, EvaluationCardDetailsDTO> evaluationCardsFirstSemester = new EnumMap<>(EvaluationPhase.class);
        Map<EvaluationPhase, EvaluationCardDetailsDTO> evaluationCardsSecondSemester = new EnumMap<>(EvaluationPhase.class);

        evaluationCardsEntities.forEach(evaluationCardEntity -> {
            EvaluationCardDetailsDTO evaluationCardDetailsDTO = createEvaluationCardDetails(evaluationCardEntity, project, evaluationCardTemplate, indexNumber);

            if (Objects.equals(Semester.FIRST, evaluationCardEntity.getSemester()) && Objects.nonNull(evaluationCardDetailsDTO)) {
                evaluationCardsFirstSemester.put(evaluationCardEntity.getEvaluationPhase(), evaluationCardDetailsDTO);
            } else if (Objects.equals(Semester.SECOND, evaluationCardEntity.getSemester()) && Objects.nonNull(evaluationCardDetailsDTO)) {
                evaluationCardsSecondSemester.put(evaluationCardEntity.getEvaluationPhase(), evaluationCardDetailsDTO);
            }
        });

        Map<Semester, Map<EvaluationPhase, EvaluationCardDetailsDTO>> evaluationCardMap = new HashMap<>();
        evaluationCardMap.put(Semester.FIRST, evaluationCardsFirstSemester);
        if (!evaluationCardsSecondSemester.isEmpty()) {
            evaluationCardMap.put(Semester.SECOND, evaluationCardsSecondSemester);
        }

        return evaluationCardMap;
    }

    private EvaluationCardDetailsDTO createEvaluationCardDetails(EvaluationCard evaluationCardEntity, Project project, EvaluationCardTemplate evaluationCardTemplate, String indexNumber) {
        if (!permissionService.isEvaluationCardVisibleForUser(evaluationCardEntity, project, indexNumber)) {
            return null;
        }
        if (projectMemberService.isStudentAMemberOfProject(indexNumber, project) && !isEvaluationCardTheMostRecentOne(project, evaluationCardEntity)) {
            // defense phase in status active should not be displayed for student - add implementation after adding freeze logic (SYSPRI-226)
            return null;
        }

        EvaluationCardDetailsDTO evaluationCardDetailsDTO = new EvaluationCardDetailsDTO();
        evaluationCardDetailsDTO.setId(evaluationCardEntity.getId());
        evaluationCardDetailsDTO.setGrade(pointsToOverallPercent(evaluationCardEntity.getTotalPoints()));

        boolean isEditable = permissionService.isEvaluationCardEditableForUser(evaluationCardEntity, project, indexNumber);

        evaluationCardDetailsDTO.setEditable(isEditable);

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

        evaluationCardDetailsDTO.setSections(sectionDTOs);
        return evaluationCardDetailsDTO;
    }

    /**
     * check if an evaluation card is the most recent one in semester
     */
    private boolean isEvaluationCardTheMostRecentOne(Project project, EvaluationCard evaluationCard) {
        Optional<EvaluationCard> theMostRecentEvaluationCard = findTheMostRecentEvaluationCard(project.getEvaluationCards(), evaluationCard.getSemester());

        return theMostRecentEvaluationCard.map(card -> Objects.equals(evaluationCard.getId(), card.getId())).orElse(Boolean.FALSE);

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
            case FIRST -> template.getCriteriaSectionsFirstSemester();
            case SECOND -> template.getCriteriaSectionsSecondSemester();
        };
    }

    // TODO: 11/18/2023 - Once SYSPRI-223 is ready, update the disqualification and approval conditions
    @Override
    @Transactional
    public UpdatedGradeDTO updateEvaluationCard(Long evaluationCardId, SingleGroupGradeUpdateDTO singleGroupGradeUpdate) {
        EvaluationCard evaluationCard = evaluationCardDAO.findById(evaluationCardId)
                .orElseThrow(() -> new EvaluationCardException(MessageFormat.format("Evaluation card with id: {0} not found", evaluationCardId)));

        Semester semester = evaluationCard.getSemester();

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

        boolean isDisqualified = checkDisqualification(gradesForSemester);
        boolean criteriaMet = !isDisqualified;
        evaluationCard.setDisqualified(isDisqualified);
        evaluationCard.setApprovedForDefense(criteriaMet);
        evaluationCardDAO.save(evaluationCard);

        return new UpdatedGradeDTO(pointsToOverallPercent(totalPointsSemester), criteriaMet);
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
    // TODO 11/23/2023: Add logic to handle different evaluation card's phase
    private boolean checkDisqualification(List<Grade> gradesForSemester) {
        return gradesForSemester.stream().filter(g -> !isGradeFromDefenseSection(g)).anyMatch(Grade::isDisqualifying) ||
                gradesForSemester.stream().filter(g -> !isGradeFromDefenseSection(g)).anyMatch(g -> Objects.isNull(g.getPointsWithWeight()));
    }

    private boolean isGradeFromDefenseSection(Grade grade) {
        return grade.getCriteriaGroup().getCriteriaSection().isDefenseSection();
    }

    @Override
    public Optional<EvaluationCard> findTheMostRecentEvaluationCard(List<EvaluationCard> evaluationCards, Semester semester) {
        Predicate<EvaluationCard> isSearchedSemester = evaluationCard -> Objects.equals(semester, evaluationCard.getSemester());
        Predicate<EvaluationCard> isDefensePhase = evaluationCard -> Objects.equals(EvaluationPhase.DEFENSE_PHASE, evaluationCard.getEvaluationPhase());
        Predicate<EvaluationCard> isRetakePhase = evaluationCard -> Objects.equals(EvaluationPhase.RETAKE_PHASE, evaluationCard.getEvaluationPhase());
        Predicate<EvaluationCard> isActiveStatus = evaluationCard -> Objects.equals(EvaluationStatus.ACTIVE, evaluationCard.getEvaluationStatus());
        Predicate<EvaluationCard> isPublishedStatus = evaluationCard -> Objects.equals(EvaluationStatus.PUBLISHED, evaluationCard.getEvaluationStatus());

        return evaluationCards.stream()
                .filter(isSearchedSemester.and(isActiveStatus))
                .findFirst()
                .or(() -> evaluationCards.stream()
                        .filter(isSearchedSemester.and(isRetakePhase).and(isPublishedStatus))
                        .findFirst()
                        .or(() -> evaluationCards.stream()
                                .filter(isSearchedSemester.and(isDefensePhase).and(isPublishedStatus))
                                .findFirst()));
    }

    @Override
    public String getPointsForSemester(Project entity, Semester semester) {
        Optional<EvaluationCard> theMostRecentEvaluationCard = findTheMostRecentEvaluationCard(entity.getEvaluationCards(), semester);
        return theMostRecentEvaluationCard.map(evaluationCard -> pointsToOverallPercent(evaluationCard.getTotalPoints())).orElse("0%");
    }

    @Override
    @Transactional
    public void publishEvaluationCard(Long evaluationCardId) {
        EvaluationCard evaluationCard = evaluationCardDAO.findById(evaluationCardId)
                .orElseThrow(() -> new EvaluationCardException(MessageFormat.format("Evaluation card with id: {0} not found", evaluationCardId)));

        evaluationCard.setEvaluationStatus(EvaluationStatus.PUBLISHED);
        log.info("Status was set to published for evaluation card with id: {}", evaluationCard.getId());
        evaluationCardDAO.save(evaluationCard);
    }

    @Override
    @Transactional
    public void publishEvaluationCards(String studyYear) {
        List<EvaluationCard> evaluationCards =
                evaluationCardDAO.findAllByEvaluationPhaseAndEvaluationStatusAndEvaluationCardTemplate_StudyYear(
                        EvaluationPhase.DEFENSE_PHASE,
                        EvaluationStatus.FROZEN,
                        studyYear
                );

        if (!evaluationCards.isEmpty()) {
            evaluationCards.forEach(e -> {
                e.setEvaluationStatus(EvaluationStatus.PUBLISHED);
                log.info("Status was set to published for evaluation card with id: {}", e.getId());
            });
            evaluationCardDAO.saveAll(evaluationCards);
        }
    }

}
