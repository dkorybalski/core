package pl.edu.amu.wmi.service.criteria.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.CriteriaGroupDAO;
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
import pl.edu.amu.wmi.service.criteria.CriteriaUpdateService;

import java.util.*;

@Service
@Transactional
@Slf4j
public class CriteriaUpdateServiceImpl implements CriteriaUpdateService {

    private static final Boolean IS_SAVE_MODE = Boolean.FALSE;

    private final CriteriaSectionMapper criteriaSectionMapper;
    private final CriteriaGroupMapper criteriaGroupMapper;
    private final CriterionMapper criterionMapper;
    private final CriteriaSectionDAO criteriaSectionDAO;
    private final CriteriaGroupDAO criteriaGroupDAO;
    private final CriterionDAO criterionDAO;

    public CriteriaUpdateServiceImpl(CriteriaSectionMapper criteriaSectionMapper, CriteriaGroupMapper criteriaGroupMapper, CriterionMapper criterionMapper, CriteriaSectionDAO criteriaSectionDAO, CriteriaGroupDAO criteriaGroupDAO, CriterionDAO criterionDAO) {
        this.criteriaSectionMapper = criteriaSectionMapper;
        this.criteriaGroupMapper = criteriaGroupMapper;
        this.criterionMapper = criterionMapper;
        this.criteriaSectionDAO = criteriaSectionDAO;
        this.criteriaGroupDAO = criteriaGroupDAO;
        this.criterionDAO = criterionDAO;
    }

    @Override
    public void updateCriteriaSection(CriteriaSectionDTO criteriaSectionDTO, EvaluationCardTemplate updatedEvaluationCardTemplate) {
        CriteriaSection firstSemesterCriteriaSection = criteriaSectionMapper.mapToEntityForFirstSemester(criteriaSectionDTO, false);
        CriteriaSection secondSemesterCriteriaSection = criteriaSectionMapper.mapToEntityForSecondSemester(criteriaSectionDTO, false);

        CriteriaSection persistedCriteriaSectionFirstSemester = criteriaSectionDAO.findById(
                firstSemesterCriteriaSection.getId()).orElseGet(CriteriaSection::new);
        CriteriaSection persistedCriteriaSectionSecondSemester = criteriaSectionDAO.findById(
                secondSemesterCriteriaSection.getId()).orElseGet(CriteriaSection::new);

        criteriaSectionMapper.update(persistedCriteriaSectionFirstSemester, firstSemesterCriteriaSection);
        criteriaSectionMapper.update(persistedCriteriaSectionSecondSemester, secondSemesterCriteriaSection);

        for (CriteriaGroupDTO criteriaGroupDTO : criteriaSectionDTO.criteriaGroups()) {
            Set<Criterion> updatedCriteria = updateCriteria(criteriaGroupDTO.criteria());

            CriteriaGroup criteriaGroupFirstSemester = updateCriteriaGroup(criteriaGroupDTO, updatedCriteria, Semester.SEMESTER_I);
            if (Objects.nonNull(criteriaGroupFirstSemester)) {
                persistedCriteriaSectionFirstSemester.getCriteriaGroups().add(criteriaGroupFirstSemester);
            }

            CriteriaGroup criteriaGroupSecondSemester = updateCriteriaGroup(criteriaGroupDTO, updatedCriteria, Semester.SEMESTER_II);
            if (Objects.nonNull(criteriaGroupSecondSemester)) {
                persistedCriteriaSectionSecondSemester.getCriteriaGroups().add(criteriaGroupSecondSemester);
            }
        }

        updatedEvaluationCardTemplate.addCriteriaSectionForFirstSemester(persistedCriteriaSectionFirstSemester);
        updatedEvaluationCardTemplate.addCriteriaSectionForSecondSemester(persistedCriteriaSectionSecondSemester);

        criteriaSectionDAO.save(persistedCriteriaSectionFirstSemester);
        criteriaSectionDAO.save(persistedCriteriaSectionSecondSemester);
    }

    private CriteriaGroup updateCriteriaGroup(CriteriaGroupDTO criteriaGroupDTO, Set<Criterion> updatedCriteria, Semester semester) {
        CriteriaGroup criteriaGroup = switch (semester) {
            case SEMESTER_I -> criteriaGroupMapper.mapToEntityForFirstSemester(criteriaGroupDTO, IS_SAVE_MODE);
            case SEMESTER_II -> criteriaGroupMapper.mapToEntityForSecondSemester(criteriaGroupDTO, IS_SAVE_MODE);
        };
        CriteriaGroup persistedCriteriaGroup = criteriaGroupDAO.findById(criteriaGroup.getId()).orElseGet(CriteriaGroup::new);
        if (!isGradeWeightRelevant(criteriaGroup)) {
            criteriaGroupDAO.delete(persistedCriteriaGroup);
            return null;
        } else {
            criteriaGroupMapper.update(persistedCriteriaGroup, criteriaGroup);
            persistedCriteriaGroup.setCriteria(updatedCriteria);
            return persistedCriteriaGroup;
        }
    }

    private boolean isGradeWeightRelevant(CriteriaGroup criteriaGroup) {
        return Objects.nonNull(criteriaGroup.getGradeWeight()) && isWeightNotZero(criteriaGroup);
    }

    private boolean isWeightNotZero(CriteriaGroup criteriaGroup) {
        return !(Math.abs(criteriaGroup.getGradeWeight()) < 1e-6);
    }

    private Set<Criterion> updateCriteria(List<CriterionDTO> criteriaDTOs) {
        List<Criterion> criteria = criterionMapper.mapToEntitiesList(criteriaDTOs, IS_SAVE_MODE);
        List<Criterion> persistedCriteria = new ArrayList<>();
        criteria.forEach(criterion -> {
            Criterion persistedEntity = criterionDAO.findById(criterion.getId()).orElseGet(Criterion::new);
            criterionMapper.update(persistedEntity, criterion);
            persistedCriteria.add(persistedEntity);
        });
        return new HashSet<>(criterionDAO.saveAll(persistedCriteria));
    }
}
