package pl.edu.amu.wmi.service.criteria.impl;

import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateRepositoryDAO;
import pl.edu.amu.wmi.entity.CriteriaGroup;
import pl.edu.amu.wmi.entity.CriteriaSection;
import pl.edu.amu.wmi.entity.Criterion;
import pl.edu.amu.wmi.entity.EvaluationCardTemplate;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.mapper.CriteriaGroupMapper;
import pl.edu.amu.wmi.mapper.EvaluationCriteriaMapper;
import pl.edu.amu.wmi.model.CriteriaGroupDTO;
import pl.edu.amu.wmi.model.CriteriaSectionDTO;
import pl.edu.amu.wmi.model.EvaluationCriteriaDTO;
import pl.edu.amu.wmi.service.criteria.EvaluationCriteriaService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class EvaluationCriteriaServiceImpl implements EvaluationCriteriaService {

    private static final Double GRADE_WEIGHT_VALUE_ZERO = 0.0;

    private final EvaluationCardTemplateRepositoryDAO evaluationCardTemplateRepositoryDAO;
    private final EvaluationCriteriaMapper evaluationCriteriaMapper;
    private final CriteriaGroupMapper criteriaGroupMapper;

    public EvaluationCriteriaServiceImpl(EvaluationCardTemplateRepositoryDAO evaluationCardTemplateRepositoryDAO, EvaluationCriteriaMapper evaluationCriteriaMapper, CriteriaGroupMapper criteriaGroupMapper) {
        this.evaluationCardTemplateRepositoryDAO = evaluationCardTemplateRepositoryDAO;
        this.evaluationCriteriaMapper = evaluationCriteriaMapper;
        this.criteriaGroupMapper = criteriaGroupMapper;
    }

    @Override
    public EvaluationCriteriaDTO constructEvaluationCriteriaDTO(String studyYear) {
        EvaluationCardTemplate evaluationCardTemplate = evaluationCardTemplateRepositoryDAO.findByStudyYear(studyYear).orElseGet(EvaluationCardTemplate::new);

        List<CriteriaSection> criteriaSectionsFirstSemester = evaluationCardTemplate.getCriteriaSectionsFirstSemester();
        List<CriteriaSection> criteriaSectionsSecondSemester = evaluationCardTemplate.getCriteriaSectionsSecondSemester();

        Set<String> criteriaSectionNames = extractCriteriaSectionNames(criteriaSectionsFirstSemester, criteriaSectionsSecondSemester);

        List<CriteriaSectionDTO> criteriaSectionDTOs = criteriaSectionNames.stream()
                .map(criteriaSectionName -> createCriteriaSectionDTO(criteriaSectionName, criteriaSectionsFirstSemester, criteriaSectionsSecondSemester))
                .toList();

        EvaluationCriteriaDTO evaluationCriteriaDTO = evaluationCriteriaMapper.mapToDto(evaluationCardTemplate);
        evaluationCriteriaDTO.criteriaSections().addAll(criteriaSectionDTOs);
        return evaluationCriteriaDTO;
    }

    private CriteriaSectionDTO createCriteriaSectionDTO(String criteriaSectionName,
                                                        List<CriteriaSection> criteriaSectionsFirstSemester,
                                                        List<CriteriaSection> criteriaSectionsSecondSemester) {

        CriteriaSection criteriaSectionFirstSemester = findCriteriaSectionByName(criteriaSectionName, criteriaSectionsFirstSemester);
        Long criteriaSectionFirstSemesterId = getCriteriaSectionId(criteriaSectionFirstSemester);
        Double criteriaSectionGradeWeightFirstSemester = getCriteriaSectionWeight(criteriaSectionFirstSemester);

        CriteriaSection criteriaSectionSecondSemester = findCriteriaSectionByName(criteriaSectionName, criteriaSectionsSecondSemester);
        Long criteriaSectionSecondSemesterId = getCriteriaSectionId(criteriaSectionSecondSemester);
        Double criteriaSectionGradeWeightSecondSemester = getCriteriaSectionWeight(criteriaSectionSecondSemester);

        Map<String, List<CriteriaGroup>> criteriaGroups = createCriteriaGroupMap(criteriaSectionFirstSemester, criteriaSectionSecondSemester);
        List<CriteriaGroupDTO> criteriaGroupDTOs = criteriaGroups.entrySet().stream()
                        .map(this::createCriteriaGroupDTO)
                        .toList();

        return new CriteriaSectionDTO(
                criteriaSectionFirstSemesterId,
                criteriaSectionSecondSemesterId,
                criteriaSectionName,
                criteriaSectionGradeWeightFirstSemester,
                criteriaSectionGradeWeightSecondSemester,
                criteriaGroupDTOs
        );
    }

    private Map<String, List<CriteriaGroup>> createCriteriaGroupMap(CriteriaSection criteriaSectionFirstSemester, CriteriaSection criteriaSectionSecondSemester) {
        Map<String, List<CriteriaGroup>> criteriaGroups = new HashMap<>();

        if (Objects.nonNull(criteriaSectionFirstSemester)) {
            criteriaSectionFirstSemester.getCriteriaGroups().forEach(criteriaGroup -> {
                criteriaGroup.setSemester(Semester.SEMESTER_I);
                criteriaGroups.computeIfAbsent(criteriaGroup.getName(), k -> new ArrayList<>()).add(criteriaGroup);
            });
        }

        if (Objects.nonNull(criteriaSectionSecondSemester)) {
            criteriaSectionSecondSemester.getCriteriaGroups().forEach(criteriaGroup -> {
                criteriaGroup.setSemester(Semester.SEMESTER_II);
                criteriaGroups.computeIfAbsent(criteriaGroup.getName(), k -> new ArrayList<>()).add(criteriaGroup);
            });
        }

        return criteriaGroups;
    }

    private Double getCriteriaSectionWeight(CriteriaSection criteriaSection) {
        return Objects.nonNull(criteriaSection) ? criteriaSection.getCriteriaSectionGradeWeight() : GRADE_WEIGHT_VALUE_ZERO;
    }

    private Long getCriteriaSectionId(CriteriaSection criteriaSection) {
        return Objects.nonNull(criteriaSection) ? criteriaSection.getId() : null;
    }

    private CriteriaGroupDTO createCriteriaGroupDTO(Map.Entry<String, List<CriteriaGroup>> entry) {
        Pair<Long, Double> criteriaGroupDataFirstSemester = getCriteriaGroupDataForSemester(entry, Semester.SEMESTER_I);
        Long idFirstSemester = criteriaGroupDataFirstSemester.getValue0();
        Double weightFirstSemester = criteriaGroupDataFirstSemester.getValue1();

        Pair<Long, Double> criteriaGroupDataSecondSemester = getCriteriaGroupDataForSemester(entry, Semester.SEMESTER_II);
        Long idSecondSemester = criteriaGroupDataSecondSemester.getValue0();
        Double weightSecondSemester = criteriaGroupDataSecondSemester.getValue1();

        List<Criterion> criteria = getCriteriaForCriteriaGroup(entry);

        return criteriaGroupMapper.mapToDto(
                entry.getKey(),
                idFirstSemester,
                idSecondSemester,
                weightFirstSemester,
                weightSecondSemester,
                criteria);
    }

    private static List<Criterion> getCriteriaForCriteriaGroup(Map.Entry<String, List<CriteriaGroup>> entry) {
        // criteria are the same for criteria groups for both semesters, therefore we take only criteria list from object with index 0
        return entry.getValue().get(0).getCriteria().stream().toList();
    }

    private Pair<Long, Double> getCriteriaGroupDataForSemester(Map.Entry<String, List<CriteriaGroup>> entry, Semester semester) {
        CriteriaGroup criteriaGroupForSemester = entry.getValue().stream()
                .filter(criteriaGroup -> Objects.equals(semester, criteriaGroup.getSemester()))
                .findFirst()
                .orElse(null);

        if (Objects.isNull(criteriaGroupForSemester)) {
            return new Pair<>(null, GRADE_WEIGHT_VALUE_ZERO);
        } else {
            return new Pair<>(criteriaGroupForSemester.getId(), criteriaGroupForSemester.getGradeWeight());
        }
    }

    private CriteriaSection findCriteriaSectionByName(String criteriaSectionName, List<CriteriaSection> criteriaSections) {
        return criteriaSections.stream()
                .filter(criteriaSection -> Objects.equals(criteriaSectionName, criteriaSection.getName()))
                .findFirst()
                .orElse(null);
    }

    private Set<String> extractCriteriaSectionNames(List<CriteriaSection> criteriaSectionsFirstSemester,
                                                    List<CriteriaSection> criteriaSectionsSecondSemester) {
        List<String> criteriaSectionFistSemesterNames = extractCriteriaSectionNamesForSemester(criteriaSectionsFirstSemester);
        List<String> criteriaSectionSecondSemesterNames = extractCriteriaSectionNamesForSemester(criteriaSectionsSecondSemester);
        return Stream.concat(criteriaSectionFistSemesterNames.stream(), criteriaSectionSecondSemesterNames.stream()).collect(Collectors.toSet());
    }

    private static List<String> extractCriteriaSectionNamesForSemester(List<CriteriaSection> criteriaSections) {
        return criteriaSections.stream()
                .map(CriteriaSection::getName)
                .toList();
    }
}
