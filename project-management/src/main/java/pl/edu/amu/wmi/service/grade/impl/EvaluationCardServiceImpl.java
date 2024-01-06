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
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.exception.grade.EvaluationCardException;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.mapper.grade.ProjectCriteriaSectionMapper;
import pl.edu.amu.wmi.model.grade.CriteriaSectionDTO;
import pl.edu.amu.wmi.model.grade.EvaluationCardDetailsDTO;
import pl.edu.amu.wmi.model.grade.SingleGroupGradeUpdateDTO;
import pl.edu.amu.wmi.model.grade.UpdatedGradeDTO;
import pl.edu.amu.wmi.service.PermissionService;
import pl.edu.amu.wmi.service.ProjectMemberService;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;
import pl.edu.amu.wmi.service.grade.GradeService;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;

import static pl.edu.amu.wmi.model.grade.GradeConstants.*;

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
    public void createEvaluationCard(Project project, String studyYear, Semester semester, EvaluationPhase phase, EvaluationStatus status, boolean isActive) {
        Optional<EvaluationCardTemplate> evaluationCardTemplate = evaluationCardTemplateDAO.findByStudyYear(studyYear);
        if (evaluationCardTemplate.isEmpty()) {
            log.info("Evaluation criteria have been not yet uploaded to the system - EvaluationCard will be updated later");
            return;
        }
        EvaluationCardTemplate template = evaluationCardTemplate.get();
        List<Grade> grades = createEmptyGrades(template, semester);

        EvaluationCard evaluationCard = new EvaluationCard();
        evaluationCard.setEvaluationCardTemplate(template);
        evaluationCard.setGrades(grades);

        evaluationCard.setSemester(semester);
        evaluationCard.setEvaluationPhase(phase);
        evaluationCard.setEvaluationStatus(status);
        evaluationCard.setTotalPoints(0.0);
        evaluationCard.setActive(isActive);

        project.addEvaluationCard(evaluationCard);

        evaluationCardDAO.save(evaluationCard);
    }

    private List<Grade> createEmptyGrades(EvaluationCardTemplate template, Semester semester) {
        List<Grade> grades = new ArrayList<>();
        switch (semester) {
            case FIRST -> grades.addAll(createEmptyGradesForSemester(template.getCriteriaSectionsFirstSemester()));
            case SECOND -> grades.addAll(createEmptyGradesForSemester(template.getCriteriaSectionsSecondSemester()));
        }
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

        if (projectMemberService.isStudentAMemberOfProject(indexNumber, project) && isAnyEvaluationCardInFrozenStatus(project.getEvaluationCards())) {
            return Collections.emptyMap();
        }

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

        Map<Semester, Map<EvaluationPhase, EvaluationCardDetailsDTO>> evaluationCardMap = new TreeMap<>();
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
        // TODO: 1/4/2024 validate if this logic is correct
        if (projectMemberService.isStudentAMemberOfProject(indexNumber, project) && isAnyEvaluationCardInFrozenStatus(project.getEvaluationCards())) {
            return null;
        }

        EvaluationCardDetailsDTO evaluationCardDetailsDTO = new EvaluationCardDetailsDTO();
        evaluationCardDetailsDTO.setId(evaluationCardEntity.getId().toString());
        evaluationCardDetailsDTO.setActive(evaluationCardEntity.isActive());
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

    private boolean isAnyEvaluationCardInFrozenStatus(List<EvaluationCard> evaluationCards) {
        return evaluationCards.stream()
                .anyMatch(evaluationCard -> Objects.equals(EvaluationStatus.FROZEN, evaluationCard.getEvaluationStatus()));
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

        boolean isDisqualified = isDisqualified(evaluationCard.getEvaluationPhase(), gradesForSemester);
        boolean criteriaMet = !isDisqualified;
        evaluationCard.setDisqualified(isDisqualified);
        evaluationCard.setApprovedForDefense(criteriaMet);
        Double finalGrade = calculateFinalGrade(criteriaMet, totalPointsSemester);
        evaluationCard.setFinalGrade(finalGrade);
        evaluationCardDAO.save(evaluationCard);

        return new UpdatedGradeDTO(pointsToOverallPercent(totalPointsSemester), criteriaMet);
    }

    private Double calculateFinalGrade(boolean criteriaMet, Double totalPointsSemester) {
        Double normalizedTotalPoints = totalPointsSemester / 4;
        double epsilon = 0.000001d; //todo add precision to comparisons
        if (Objects.equals(Boolean.FALSE, criteriaMet) || normalizedTotalPoints < GRADE_3_0_MIN_THRESHOLD) {
            return GRADE_2_0;
        } else if (normalizedTotalPoints >= GRADE_3_0_MIN_THRESHOLD && normalizedTotalPoints < GRADE_3_5_MIN_THRESHOLD) {
            return GRADE_3_0;
        } else if (normalizedTotalPoints >= GRADE_3_5_MIN_THRESHOLD && normalizedTotalPoints < GRADE_4_0_MIN_THRESHOLD) {
            return GRADE_3_5;
        } else if (normalizedTotalPoints >= GRADE_4_0_MIN_THRESHOLD && normalizedTotalPoints < GRADE_4_5_MIN_THRESHOLD) {
            return GRADE_4_0;
        } else if (normalizedTotalPoints >= GRADE_4_5_MIN_THRESHOLD && normalizedTotalPoints < GRADE_5_0_MIN_THRESHOLD) {
            return GRADE_4_5;
        } else if (normalizedTotalPoints >= GRADE_5_0_MIN_THRESHOLD) {
            return GRADE_5_0;
        } else {
            return 0.0;
        }
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
     * Check, taking into account the grading phase, that all grades have been selected and none of them are disqualifying.
     * If there is no single grade selected or one of them is disqualifying, the entire project is disqualified.
     */
    private boolean isDisqualified(EvaluationPhase evaluationPhase, List<Grade> gradesForSemester) {
        if (evaluationPhase.equals(EvaluationPhase.SEMESTER_PHASE))
            return isDisqualifiedWithoutDefenseSection(gradesForSemester);
        else
            return isDisqualifiedIncludingDefenseSection(gradesForSemester);
    }

    private boolean isDisqualifiedWithoutDefenseSection(List<Grade> gradesForSemester) {
        return isDisqualifiedBySelectedGradeWithoutDefenseSection(gradesForSemester) ||
                isDisqualifiedByNotSelectedGradeWithoutDefenseSection(gradesForSemester);
    }

    private boolean isDisqualifiedBySelectedGradeWithoutDefenseSection(List<Grade> gradesForSemester) {
        return gradesForSemester.stream().filter(g -> !isGradeFromDefenseSection(g)).anyMatch(Grade::isDisqualifying);
    }

    private boolean isDisqualifiedByNotSelectedGradeWithoutDefenseSection(List<Grade> gradesForSemester) {
        return gradesForSemester.stream().filter(g -> !isGradeFromDefenseSection(g)).anyMatch(g -> Objects.isNull(g.getPointsWithWeight()));
    }

    private boolean isGradeFromDefenseSection(Grade grade) {
        return grade.getCriteriaGroup().getCriteriaSection().isDefenseSection();
    }

    private boolean isDisqualifiedIncludingDefenseSection(List<Grade> gradesForSemester) {
        return isDisqualifiedBySelectedGradeIncludingDefenseSection(gradesForSemester) ||
                isDisqualifiedByNotSelectedGradeIncludingDefenseSection(gradesForSemester);
    }

    private boolean isDisqualifiedBySelectedGradeIncludingDefenseSection(List<Grade> gradesForSemester) {
        return gradesForSemester.stream().anyMatch(Grade::isDisqualifying);
    }

    private boolean isDisqualifiedByNotSelectedGradeIncludingDefenseSection(List<Grade> gradesForSemester) {
        return gradesForSemester.stream().anyMatch(g -> Objects.isNull(g.getPointsWithWeight()));
    }

    @Override
    public Optional<EvaluationCard> findTheMostRecentEvaluationCard(List<EvaluationCard> evaluationCards, Semester semester) {
        if (Objects.isNull(semester)) {
            return evaluationCards.stream()
                    .filter(evaluationCard -> Objects.equals(Boolean.TRUE, evaluationCard.isActive()))
                    .findFirst();
        } else {
            Predicate<EvaluationCard> isSearchedSemester = evaluationCard -> Objects.equals(semester, evaluationCard.getSemester());
            Predicate<EvaluationCard> isDefensePhase = evaluationCard -> Objects.equals(EvaluationPhase.DEFENSE_PHASE, evaluationCard.getEvaluationPhase());
            Predicate<EvaluationCard> isRetakePhase = evaluationCard -> Objects.equals(EvaluationPhase.RETAKE_PHASE, evaluationCard.getEvaluationPhase());
            Predicate<EvaluationCard> isActiveStatus = evaluationCard -> Objects.equals(EvaluationStatus.ACTIVE, evaluationCard.getEvaluationStatus());

            return evaluationCards.stream()
                    .filter(isSearchedSemester.and(isActiveStatus))
                    .findFirst()
                    .or(() -> evaluationCards.stream()
                            .filter(isSearchedSemester.and(isRetakePhase))
                            .findFirst()
                            .or(() -> evaluationCards.stream()
                                    .filter(isSearchedSemester.and(isDefensePhase))
                                    .findFirst()));
        }
    }

    @Override
    public String getPointsForSemester(Project entity, Semester semester) {
        Optional<EvaluationCard> theMostRecentEvaluationCard = findTheMostRecentEvaluationCard(entity.getEvaluationCards(), semester);
        return theMostRecentEvaluationCard.map(evaluationCard -> pointsToOverallPercent(evaluationCard.getTotalPoints())).orElse("0,00%");
    }

    @Override
    @Transactional
    public void publishEvaluationCard(Long projectId) {
        publishEvaluationCardsForProject(projectId);
    }

    private void publishEvaluationCardsForProject(Long projectId) {
        List<EvaluationCard> evaluationCards = evaluationCardDAO.findAllByProject_Id(projectId);

        evaluationCards.stream()
                .filter(evaluationCard -> isEvaluationCardFrozen(evaluationCard) || isEvaluationCardInActiveDefensePhase(evaluationCard))
                .forEach(evaluationCard -> {
                    evaluationCard.setEvaluationStatus(EvaluationStatus.PUBLISHED);
                    log.info("Status was set to published for evaluation card with id: {}", evaluationCard.getId());
                    evaluationCardDAO.save(evaluationCard);
                });
    }

    private static boolean isEvaluationCardInActiveDefensePhase(EvaluationCard evaluationCard) {
        return Objects.equals(EvaluationPhase.DEFENSE_PHASE, evaluationCard.getEvaluationPhase())
                && Objects.equals(EvaluationStatus.ACTIVE, evaluationCard.getEvaluationStatus());
    }

    private static boolean isEvaluationCardFrozen(EvaluationCard evaluationCard) {
        return Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCard.getEvaluationPhase())
                && Objects.equals(EvaluationStatus.FROZEN, evaluationCard.getEvaluationStatus());
    }

    @Override
    @Transactional
    public void publishEvaluationCards(String studyYear) {
        List<Long> projectIds = projectDAO.findProjectIdsByStudyYear(studyYear);
        projectIds.forEach(this::publishEvaluationCardsForProject);
    }

    @Override
    @Transactional
    public void freezeEvaluationCard(Long projectId) {
        Project project = projectDAO.findById(projectId)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        EvaluationCard semesterEvaluationCard = project.getEvaluationCards().stream()
                .filter(evaluationCard -> Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCard.getEvaluationPhase()))
                .filter(evaluationCard -> Objects.equals(EvaluationStatus.ACTIVE, evaluationCard.getEvaluationStatus()))
                .filter(evaluationCard -> Objects.equals(Boolean.TRUE, evaluationCard.isActive()))
                .findFirst().orElseThrow(() -> new BusinessException(MessageFormat.format(
                        "Evaluation card in semester phase for project with id: {0} not found. Defense card cannot be created.", projectId)));

        EvaluationCard defenseEvaluationCard = createModifiedEvaluationCardCopy(semesterEvaluationCard, EvaluationPhase.DEFENSE_PHASE, EvaluationStatus.ACTIVE);

        project.addEvaluationCard(defenseEvaluationCard);
        evaluationCardDAO.save(defenseEvaluationCard);

        semesterEvaluationCard.setEvaluationStatus(EvaluationStatus.FROZEN);
        semesterEvaluationCard.setActive(Boolean.FALSE);
        evaluationCardDAO.save(semesterEvaluationCard);
    }

    @Override
    @Transactional
    public void retakeEvaluationCard(Long projectId) {
        Project project = projectDAO.findById(projectId)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        EvaluationCard defenseEvaluationCard = project.getEvaluationCards().stream()
                .filter(evaluationCard -> Objects.equals(EvaluationPhase.DEFENSE_PHASE, evaluationCard.getEvaluationPhase()))
                .filter(evaluationCard -> Objects.equals(EvaluationStatus.PUBLISHED, evaluationCard.getEvaluationStatus()))
                .filter(evaluationCard -> Objects.equals(Boolean.TRUE, evaluationCard.isActive()))
                .findFirst().orElseThrow(() -> new BusinessException(MessageFormat.format(
                        "Evaluation card in defense phase for project with id: {0} not found. Retake card cannot be created.", projectId)));

        EvaluationCard retakeEvaluationCard = createModifiedEvaluationCardCopy(defenseEvaluationCard, EvaluationPhase.RETAKE_PHASE, EvaluationStatus.ACTIVE);

        project.addEvaluationCard(retakeEvaluationCard);
        evaluationCardDAO.save(retakeEvaluationCard);

        defenseEvaluationCard.setActive(Boolean.FALSE);
        evaluationCardDAO.save(defenseEvaluationCard);
    }

    @Override
    @Transactional
    public void activateEvaluationCardsForSecondSemester(String studyYear) {
        List<Long> projectIds = projectDAO.findProjectIdsByStudyYear(studyYear);
        projectIds.forEach(this::activateEvaluationCardForSecondSemesterForProject);
    }

    private void activateEvaluationCardForSecondSemesterForProject(Long projectId) {
        List<EvaluationCard> evaluationCards = evaluationCardDAO.findAllByProject_Id(projectId);
        if (isSecondSemesterCardActivationRequired(evaluationCards)) {
            evaluationCards.forEach(evaluationCard -> {
                if (isACardForSecondSemesterToBeAcivated(evaluationCard)) {
                    evaluationCard.setEvaluationStatus(EvaluationStatus.ACTIVE);
                    evaluationCard.setActive(Boolean.TRUE);
                    evaluationCardDAO.save(evaluationCard);
                } else if (Objects.equals(Boolean.TRUE, evaluationCard.isActive())) {
                    evaluationCard.setActive(Boolean.FALSE);
                    evaluationCardDAO.save(evaluationCard);
                }
            });
            log.info("Evaluation card for second semester was activated for a project: {}", projectId);
        } else {
            log.info("Second semester evaluation card activation is not relevant for a project: {}", projectId);
        }
    }

    private boolean isSecondSemesterCardActivationRequired(List<EvaluationCard> evaluationCards) {
        return evaluationCards.stream()
                .anyMatch(EvaluationCardServiceImpl::isACardForSecondSemesterToBeAcivated);
    }

    private static boolean isACardForSecondSemesterToBeAcivated(EvaluationCard evaluationCard) {
        return Objects.equals(EvaluationPhase.SEMESTER_PHASE, evaluationCard.getEvaluationPhase())
                && Objects.equals(EvaluationStatus.INACTIVE, evaluationCard.getEvaluationStatus())
                && Objects.equals(Semester.SECOND, evaluationCard.getSemester());
    }

    private EvaluationCard createModifiedEvaluationCardCopy(EvaluationCard originalEvaluationCard, EvaluationPhase phase, EvaluationStatus status) {
        EvaluationCard modifiedCopy = new EvaluationCard();
        modifiedCopy.setEvaluationCardTemplate(originalEvaluationCard.getEvaluationCardTemplate());
        List<Grade> semesterGrades = originalEvaluationCard.getGrades();
        List<Grade> defenseCopiedGrades = new ArrayList<>();

        semesterGrades.forEach(semesterGrade -> {
            Grade clonedGrade = semesterGrade.createACopy();
            defenseCopiedGrades.add(clonedGrade);
        });

        modifiedCopy.setGrades(defenseCopiedGrades);
        modifiedCopy.setSemester(originalEvaluationCard.getSemester());
        modifiedCopy.setEvaluationPhase(phase);
        modifiedCopy.setEvaluationStatus(status);
        modifiedCopy.setTotalPoints(originalEvaluationCard.getTotalPoints());
        modifiedCopy.setActive(Boolean.TRUE);
        return modifiedCopy;
    }

}
