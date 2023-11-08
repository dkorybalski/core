package pl.edu.amu.wmi.service.criteria.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.CriteriaSectionDAO;
import pl.edu.amu.wmi.dao.CriterionDAO;
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
import pl.edu.amu.wmi.service.criteria.CriteriaSaveService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class CriteriaSaveServiceImpl implements CriteriaSaveService {

    private static final Boolean IS_SAVE_MODE = Boolean.TRUE;

    private final CriteriaSectionMapper criteriaSectionMapper;
    private final CriteriaGroupMapper criteriaGroupMapper;
    private final CriterionMapper criterionMapper;
    private final CriteriaSectionDAO criteriaSectionDAO;
    private final CriterionDAO criterionDAO;

    public CriteriaSaveServiceImpl(CriteriaSectionMapper criteriaSectionMapper,
                                   CriteriaGroupMapper criteriaGroupMapper,
                                   CriterionMapper criterionMapper,
                                   CriteriaSectionDAO criteriaSectionDAO,
                                   CriterionDAO criterionDAO) {
        this.criteriaSectionMapper = criteriaSectionMapper;
        this.criteriaGroupMapper = criteriaGroupMapper;
        this.criterionMapper = criterionMapper;
        this.criteriaSectionDAO = criteriaSectionDAO;
        this.criterionDAO = criterionDAO;
    }

    @Override
    public void saveCriteriaSection(CriteriaSectionDTO criteriaSectionDTO, EvaluationCardTemplate savedEvaluationCardTemplate) {
        CriteriaSection firstSemesterCriteriaSection = criteriaSectionMapper.mapToEntityForFirstSemester(criteriaSectionDTO, IS_SAVE_MODE);
        CriteriaSection secondSemesterCriteriaSection = criteriaSectionMapper.mapToEntityForSecondSemester(criteriaSectionDTO, IS_SAVE_MODE);

        for (CriteriaGroupDTO criteriaGroupDTO : criteriaSectionDTO.criteriaGroups()) {
            Set<Criterion> savedCriteria = saveCriteria(criteriaGroupDTO.criteria());

            CriteriaGroup criteriaGroupForFirstSemester = createCriteriaGroup(criteriaGroupDTO, savedCriteria, Semester.SEMESTER_I);
            if (Objects.nonNull(criteriaGroupForFirstSemester)) {
                firstSemesterCriteriaSection.getCriteriaGroups().add(criteriaGroupForFirstSemester);
            }

            CriteriaGroup criteriaGroupForSecondSemester = createCriteriaGroup(criteriaGroupDTO, savedCriteria, Semester.SEMESTER_II);
            if (Objects.nonNull(criteriaGroupForSecondSemester)) {
                secondSemesterCriteriaSection.getCriteriaGroups().add(criteriaGroupForSecondSemester);
            }
        }

        savedEvaluationCardTemplate.addCriteriaSectionForFirstSemester(firstSemesterCriteriaSection);
        savedEvaluationCardTemplate.addCriteriaSectionForSecondSemester(secondSemesterCriteriaSection);

        criteriaSectionDAO.save(firstSemesterCriteriaSection);
        criteriaSectionDAO.save(secondSemesterCriteriaSection);
    }

    private CriteriaGroup createCriteriaGroup(CriteriaGroupDTO criteriaGroupDTO, Set<Criterion> savedCriteria, Semester semester) {
        CriteriaGroup criteriaGroup = switch (semester) {
            case SEMESTER_I -> criteriaGroupMapper.mapToEntityForFirstSemester(criteriaGroupDTO, IS_SAVE_MODE);
            case SEMESTER_II -> criteriaGroupMapper.mapToEntityForSecondSemester(criteriaGroupDTO, IS_SAVE_MODE);
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

    private Set<Criterion> saveCriteria(List<CriterionDTO> criterionDTOS) {
        List<Criterion> criteria = criterionMapper.mapToEntitiesList(criterionDTOS, IS_SAVE_MODE);
        return new HashSet<>(criterionDAO.saveAll(criteria));
    }
}
