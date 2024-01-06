package pl.edu.amu.wmi.service.grade.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.amu.wmi.dao.EvaluationCardDAO;
import pl.edu.amu.wmi.dao.EvaluationCardTemplateDAO;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.CriterionCategory;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.mapper.grade.ProjectCriteriaSectionMapper;
import pl.edu.amu.wmi.service.PermissionService;
import pl.edu.amu.wmi.service.ProjectMemberService;
import pl.edu.amu.wmi.service.grade.GradeService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.edu.amu.wmi.utils.Constants.STUDY_YEAR_FULL_TIME_2023;

@ExtendWith(MockitoExtension.class)
class EvaluationCardServiceImplTest {

    @Mock
    private EvaluationCardDAO evaluationCardDAO;
    @Mock
    private EvaluationCardTemplateDAO evaluationCardTemplateDAO;
    @Mock
    private ProjectDAO projectDAO;
    @Mock
    private GradeService gradeService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private ProjectMemberService projectMemberService;
    @Mock
    private ProjectCriteriaSectionMapper projectCriteriaSectionMapper;

    @InjectMocks
    private EvaluationCardServiceImpl evaluationCardService;

    @Test
    void createEvaluationCard_successful() {
        //given
        int numberOfSections = 2;
        int numberOfGroupsPerSection = 3;
        int expectedNumberOfGrades = numberOfSections * numberOfGroupsPerSection;
        EvaluationCardTemplate evaluationCardTemplate = createEvaluationCardTemplate(numberOfSections, numberOfGroupsPerSection);
        Mockito.when(evaluationCardTemplateDAO.findByStudyYear(STUDY_YEAR_FULL_TIME_2023)).thenReturn(Optional.of(evaluationCardTemplate));
        Project project = createProject();
        //when
        evaluationCardService.createEvaluationCard(project, STUDY_YEAR_FULL_TIME_2023, Semester.FIRST, EvaluationPhase.SEMESTER_PHASE, EvaluationStatus.ACTIVE, Boolean.TRUE);
        //then
        ArgumentCaptor<EvaluationCard> captor = ArgumentCaptor.forClass(EvaluationCard.class);
        Mockito.verify(evaluationCardTemplateDAO, Mockito.times(1)).findByStudyYear(STUDY_YEAR_FULL_TIME_2023);
        Mockito.verify(evaluationCardDAO, Mockito.times(1)).save(captor.capture());

        EvaluationCard evaluationCard = captor.getValue();
        assertThat(evaluationCard.getGrades()).hasSize(expectedNumberOfGrades);
        assertThat(evaluationCard.getSemester()).isEqualTo(Semester.FIRST);
        assertThat(evaluationCard.getEvaluationPhase()).isEqualTo(EvaluationPhase.SEMESTER_PHASE);
        assertThat(evaluationCard.getEvaluationStatus()).isEqualTo(EvaluationStatus.ACTIVE);
    }

    private Project createProject() {
        Project project = new Project();
        project.setId(1L);
        return project;
    }

    private EvaluationCardTemplate createEvaluationCardTemplate(int numberOfSection, int numberOfGroupsPerSection) {
        EvaluationCardTemplate evaluationCardTemplate = new EvaluationCardTemplate();
        evaluationCardTemplate.setStudyYear(STUDY_YEAR_FULL_TIME_2023);
        evaluationCardTemplate.setId(1L);
        List<CriteriaSection> criteriaSectionsFirstSemester = createCriteriaSections(numberOfSection, numberOfGroupsPerSection, evaluationCardTemplate, Semester.FIRST);
        evaluationCardTemplate.setCriteriaSectionsFirstSemester(criteriaSectionsFirstSemester);

        List<CriteriaSection> criteriaSectionsSecondSemester = createCriteriaSections(numberOfSection, numberOfGroupsPerSection, evaluationCardTemplate, Semester.SECOND);
        evaluationCardTemplate.setCriteriaSectionsSecondSemester(criteriaSectionsSecondSemester);

        return evaluationCardTemplate;
    }

    private List<CriteriaSection> createCriteriaSections(int numberOfSection, int numberOfGroupsPerSection, EvaluationCardTemplate evaluationCardTemplate, Semester semester) {
        List<CriteriaSection> criteriaSections = new ArrayList<>();
        for (int i = 0; i < numberOfSection; i++) {
            CriteriaSection criteriaSection = createCriteriaSection(evaluationCardTemplate, numberOfGroupsPerSection, semester);
            criteriaSections.add(criteriaSection);
        }
        return criteriaSections;
    }

    private CriteriaSection createCriteriaSection(EvaluationCardTemplate evaluationCardTemplate, int numberOfGroupsPerSection, Semester semester) {
        CriteriaSection criteriaSection = new CriteriaSection();
        criteriaSection.setEvaluationCardTemplate(evaluationCardTemplate);
        criteriaSection.setName("Criteria section");

        List<CriteriaGroup> criteriaGroups = new ArrayList<>();
        for (int i = 0; i < numberOfGroupsPerSection; i++) {
            CriteriaGroup criteriaGroup = createCriteriaGroup(criteriaSection, semester);
            criteriaGroups.add(criteriaGroup);
        }

        criteriaSection.setCriteriaGroups(criteriaGroups);
        return criteriaSection;
    }

    private CriteriaGroup createCriteriaGroup(CriteriaSection criteriaSection, Semester semester) {
        CriteriaGroup criteriaGroup = new CriteriaGroup();
        criteriaGroup.setName("CriteriaGroup");
        criteriaGroup.setSemester(semester);
        criteriaGroup.setGradeWeight(0.05);
        criteriaGroup.setCriteriaSection(criteriaSection);
        Set<Criterion> criteria = createCriteria();
        criteriaGroup.setCriteria(criteria);
        return criteriaGroup;
    }

    private Set<Criterion> createCriteria() {
        Set<Criterion> criteria = new HashSet<>();
        Criterion criterion = new Criterion();
        criterion.setCriterionCategory(CriterionCategory.CRITERION_MET);
        criterion.setDescription("CriterionDescription");
        criterion.setDisqualifying(false);
        criteria.add(criterion);
        return criteria;
    }

}
