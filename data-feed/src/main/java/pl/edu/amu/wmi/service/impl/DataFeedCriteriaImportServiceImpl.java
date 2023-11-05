package pl.edu.amu.wmi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.dao.CriteriaGroupDAO;
import pl.edu.amu.wmi.dao.EvaluationCardRepositoryDAO;
import pl.edu.amu.wmi.dao.ScoringCriteriaDAO;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.entity.ScoringCriteria;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.mapper.CriteriaGroupMapper;
import pl.edu.amu.wmi.mapper.CriterionMapper;
import pl.edu.amu.wmi.mapper.ScoringCriteriaMapper;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;
import pl.edu.amu.wmi.model.CriterionDTO;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;
import pl.edu.amu.wmi.model.ScoringCriteriaDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.DataFeedImportService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
public class DataFeedCriteriaImportServiceImpl implements DataFeedImportService {

    private final CriteriaGroupMapper criteriaGroupMapper;
    private final CriterionMapper criterionMapper;
    private final ScoringCriteriaMapper scoringCriteriaMapper;
    private final CriteriaGroupDAO criteriaGroupDAO;
    private final ScoringCriteriaDAO scoringCriteriaDAO;
    private final EvaluationCardRepositoryDAO evaluationCardRepositoryDAO;

    public DataFeedCriteriaImportServiceImpl(CriteriaGroupMapper criteriaGroupMapper, CriterionMapper criterionMapper, ScoringCriteriaMapper scoringCriteriaMapper, CriteriaGroupDAO criteriaGroupDAO, ScoringCriteriaDAO scoringCriteriaDAO, EvaluationCardRepositoryDAO evaluationCardRepositoryDAO) {
        this.criteriaGroupMapper = criteriaGroupMapper;
        this.criterionMapper = criterionMapper;
        this.scoringCriteriaMapper = scoringCriteriaMapper;
        this.criteriaGroupDAO = criteriaGroupDAO;
        this.scoringCriteriaDAO = scoringCriteriaDAO;
        this.evaluationCardRepositoryDAO = evaluationCardRepositoryDAO;
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.NEW_CRITERIA;
    }

    @Override
    @Transactional
    public void saveRecords(MultipartFile data, String studyYear) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            EvaluationCriteriaDTO evaluationCriteriaDTO = objectMapper.readValue(data.getInputStream(), EvaluationCriteriaDTO.class);
            if (!Objects.equals(studyYear, evaluationCriteriaDTO.studyYear())) {
                log.warn("Study year from file is different that active one. Study year from file is ignored");
            }
            List<CriteriaGroupDTO> criteriaGroups = evaluationCriteriaDTO.criteriaGroups();

            EvaluationCardTemplate evaluationCardTemplateForStudyYear =
                    evaluationCardRepositoryDAO.findByStudyYear(studyYear).orElse(null);
            if (Objects.isNull(evaluationCardTemplateForStudyYear)) {
                EvaluationCardTemplate evaluationCardTemplate = saveEvaluationCardTemplate(studyYear, evaluationCriteriaDTO);
                criteriaGroups.forEach(criteriaGroup -> saveCriteriaGroup(criteriaGroup, evaluationCardTemplate, true));
            } else {
                EvaluationCardTemplate updatedEvaluationCardTemplate = updateEvaluationCardTemplate(evaluationCardTemplateForStudyYear, evaluationCriteriaDTO);
                // TODO: 11/5/2023 implement logic
            }
        } catch (Exception exception) {
            log.error("Exception during parsing the criteria");
            throw exception;
        }
    }

    private EvaluationCardTemplate updateEvaluationCardTemplate(EvaluationCardTemplate evaluationCardTemplateForStudyYear, EvaluationCriteriaDTO evaluationCriteriaDTO) {
        evaluationCardTemplateForStudyYear.setMinPointsThresholdFirstSemester(evaluationCriteriaDTO.minPointsThresholdFirstSemester());
        evaluationCardTemplateForStudyYear.setMinPointsThresholdSecondSemester(evaluationCriteriaDTO.minPointsThresholdSecondSemester());
        return evaluationCardRepositoryDAO.save(evaluationCardTemplateForStudyYear);
    }

    private EvaluationCardTemplate saveEvaluationCardTemplate(String studyYear, EvaluationCriteriaDTO evaluationCriteriaDTO) {
        EvaluationCardTemplate evaluationCardTemplate = new EvaluationCardTemplate();
        evaluationCardTemplate.setStudyYear(studyYear);
        evaluationCardTemplate.setMinPointsThresholdFirstSemester(evaluationCriteriaDTO.minPointsThresholdFirstSemester());
        evaluationCardTemplate.setMinPointsThresholdSecondSemester(evaluationCriteriaDTO.minPointsThresholdSecondSemester());
        return evaluationCardRepositoryDAO.save(evaluationCardTemplate);
    }

    private void saveCriteriaGroup(CriteriaGroupDTO criteriaGroupDTO, EvaluationCardTemplate savedEvaluationCardTemplate, boolean isNewEvaluation) {
        CriteriaGroup firstSemesterCriteriaGroup = criteriaGroupMapper.mapToEntityForFirstSemester(criteriaGroupDTO, isNewEvaluation);
        CriteriaGroup secondSemesterCriteriaGroup = criteriaGroupMapper.mapToEntityForSecondSemester(criteriaGroupDTO, isNewEvaluation);

        for (CriterionDTO criterionDTO : criteriaGroupDTO.criteria()) {
            Set<ScoringCriteria> savedScoringCriteria = saveScoringCriteria(criterionDTO.scoringCriteria(), isNewEvaluation);

            Criterion criterionForFirstSemester = createCriterion(criterionDTO, savedScoringCriteria, Semester.SEMESTER_I, isNewEvaluation);
            if (Objects.nonNull(criterionForFirstSemester)) {
                firstSemesterCriteriaGroup.getCriteria().add(criterionForFirstSemester);
            }

            Criterion criterionForSecondSemester = createCriterion(criterionDTO, savedScoringCriteria, Semester.SEMESTER_II, isNewEvaluation);
            if (Objects.nonNull(criterionForSecondSemester)) {
                secondSemesterCriteriaGroup.getCriteria().add(criterionForSecondSemester);
            }
        }

        savedEvaluationCardTemplate.addCriteriaGroupForFirstSemester(firstSemesterCriteriaGroup);
        savedEvaluationCardTemplate.addCriteriaGroupForSecondSemester(secondSemesterCriteriaGroup);

        criteriaGroupDAO.save(firstSemesterCriteriaGroup);
        criteriaGroupDAO.save(secondSemesterCriteriaGroup);
    }

    private Criterion createCriterion(CriterionDTO criterionDTO, Set<ScoringCriteria> savedScoringCriteria, Semester semester, boolean isNewEvaluation) {
        Criterion criterion = switch (semester) {
            case SEMESTER_I -> criterionMapper.mapToEntityForFirstSemester(criterionDTO, isNewEvaluation);
            case SEMESTER_II -> criterionMapper.mapToEntityForSecondSemester(criterionDTO, isNewEvaluation);
        };
        criterion.setScoringCriteria(savedScoringCriteria);
        return isGradeWeightRelevant(criterion) ? criterion : null;
    }

    private boolean isGradeWeightRelevant(Criterion criterion) {
        return Objects.nonNull(criterion.getGradeWeight()) && isWeightNotZero(criterion);
    }

    private boolean isWeightNotZero(Criterion criterion) {
        return !(Math.abs(criterion.getGradeWeight()) < 1e-6);
    }

    private Set<ScoringCriteria> saveScoringCriteria(List<ScoringCriteriaDTO> scoringCriteriaDTOs, boolean isNewEvaluation) {
        List<ScoringCriteria> scoringCriteria = scoringCriteriaMapper.mapToEntitiesList(scoringCriteriaDTOs, isNewEvaluation);
        return new HashSet<>(scoringCriteriaDAO.saveAll(scoringCriteria));
    }
}
