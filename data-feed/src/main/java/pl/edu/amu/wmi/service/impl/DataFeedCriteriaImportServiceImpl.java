package pl.edu.amu.wmi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.amu.wmi.dao.CriteriaDAO;
import pl.edu.amu.wmi.dao.CriteriaSectionDAO;
import pl.edu.amu.wmi.dao.EvaluationCardRepositoryDAO;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.entity.CriteriaSection;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.mapper.CriteriaGroupMapper;
import pl.edu.amu.wmi.mapper.CriteriaSectionMapper;
import pl.edu.amu.wmi.mapper.CriterionMapper;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;
import pl.edu.amu.wmi.model.CriteriaSectionDTO;
import pl.edu.amu.wmi.model.CriterionDTO;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;
import pl.edu.amu.wmi.model.enumeration.DataFeedType;
import pl.edu.amu.wmi.service.DataFeedImportService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
public class DataFeedCriteriaImportServiceImpl implements DataFeedImportService {

    private final CriteriaSectionMapper criteriaSectionMapper;
    private final CriteriaGroupMapper criteriaGroupMapper;
    private final CriterionMapper criterionMapper;
    private final CriteriaSectionDAO criteriaSectionDAO;
    private final CriteriaDAO criteriaDAO;
    private final EvaluationCardRepositoryDAO evaluationCardRepositoryDAO;

    public DataFeedCriteriaImportServiceImpl(CriteriaSectionMapper criteriaSectionMapper, CriteriaGroupMapper criteriaGroupMapper, CriterionMapper criterionMapper, CriteriaSectionDAO criteriaSectionDAO, CriteriaDAO criteriaDAO, EvaluationCardRepositoryDAO evaluationCardRepositoryDAO) {
        this.criteriaSectionMapper = criteriaSectionMapper;
        this.criteriaGroupMapper = criteriaGroupMapper;
        this.criterionMapper = criterionMapper;
        this.criteriaSectionDAO = criteriaSectionDAO;
        this.criteriaDAO = criteriaDAO;
        this.evaluationCardRepositoryDAO = evaluationCardRepositoryDAO;
    }

    @Override
    public DataFeedType getType() {
        return DataFeedType.CRITERIA;
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
            List<CriteriaSectionDTO> criteriaSections = evaluationCriteriaDTO.criteriaSections();

            EvaluationCardTemplate evaluationCardTemplateForStudyYear =
                    evaluationCardRepositoryDAO.findByStudyYear(studyYear).orElse(null);
            if (Objects.isNull(evaluationCardTemplateForStudyYear)) {
                EvaluationCardTemplate evaluationCardTemplate = saveEvaluationCardTemplate(studyYear, evaluationCriteriaDTO);
                criteriaSections.forEach(criteriaSection -> saveCriteriaSection(criteriaSection, evaluationCardTemplate, true));
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

    private void saveCriteriaSection(CriteriaSectionDTO criteriaSectionDTO, EvaluationCardTemplate savedEvaluationCardTemplate, boolean isNewEvaluation) {
        CriteriaSection firstSemesterCriteriaSection = criteriaSectionMapper.mapToEntityForFirstSemester(criteriaSectionDTO, isNewEvaluation);
        CriteriaSection secondSemesterCriteriaSection = criteriaSectionMapper.mapToEntityForSecondSemester(criteriaSectionDTO, isNewEvaluation);

        for (CriteriaGroupDTO criteriaGroupDTO : criteriaSectionDTO.criteriaGroups()) {
            Set<Criterion> savedCriteria = saveCriteria(criteriaGroupDTO.criteria(), isNewEvaluation);

            CriteriaGroup criteriaGroupForFirstSemester = createCriteriaGroup(criteriaGroupDTO, savedCriteria, Semester.SEMESTER_I, isNewEvaluation);
            if (Objects.nonNull(criteriaGroupForFirstSemester)) {
                firstSemesterCriteriaSection.getCriteriaGroups().add(criteriaGroupForFirstSemester);
            }

            CriteriaGroup criteriaGroupForSecondSemester = createCriteriaGroup(criteriaGroupDTO, savedCriteria, Semester.SEMESTER_II, isNewEvaluation);
            if (Objects.nonNull(criteriaGroupForSecondSemester)) {
                secondSemesterCriteriaSection.getCriteriaGroups().add(criteriaGroupForSecondSemester);
            }
        }

        savedEvaluationCardTemplate.addCriteriaSectionForFirstSemester(firstSemesterCriteriaSection);
        savedEvaluationCardTemplate.addCriteriaSectionForSecondSemester(secondSemesterCriteriaSection);

        criteriaSectionDAO.save(firstSemesterCriteriaSection);
        criteriaSectionDAO.save(secondSemesterCriteriaSection);
    }

    private CriteriaGroup createCriteriaGroup(CriteriaGroupDTO criteriaGroupDTO, Set<Criterion> savedCriteria, Semester semester, boolean isNewEvaluation) {
        CriteriaGroup criteriaGroup = switch (semester) {
            case SEMESTER_I -> criteriaGroupMapper.mapToEntityForFirstSemester(criteriaGroupDTO, isNewEvaluation);
            case SEMESTER_II -> criteriaGroupMapper.mapToEntityForSecondSemester(criteriaGroupDTO, isNewEvaluation);
        };
        criteriaGroup.setCriteria(savedCriteria);
        return isGradeWeightRelevant(criteriaGroup) ? criteriaGroup : null;
    }

    private boolean isGradeWeightRelevant(CriteriaGroup criteriaGroup) {
        return Objects.nonNull(criteriaGroup.getGradeWeight()) && isWeightNotZero(criteriaGroup);
    }

    private boolean isWeightNotZero(CriteriaGroup criteriaGroup) {
        return !(Math.abs(criteriaGroup.getGradeWeight()) < 1e-6);
    }

    private Set<Criterion> saveCriteria(List<CriterionDTO> criterionDTOS, boolean isNewEvaluation) {
        List<Criterion> criteria = criterionMapper.mapToEntitiesList(criterionDTOS, isNewEvaluation);
        return new HashSet<>(criteriaDAO.saveAll(criteria));
    }
}
