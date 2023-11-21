package pl.edu.amu.wmi.service.grade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.CriterionCategory;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.mapper.grade.ProjectCriteriaSectionMapper;
import pl.edu.amu.wmi.model.grade.CriteriaSectionDTO;
import pl.edu.amu.wmi.model.grade.GradeDetailsDTO;
import pl.edu.amu.wmi.service.grade.GradeService;

import java.text.MessageFormat;
import java.util.*;

@Service
public class GradeServiceImpl implements GradeService {

    private final ProjectDAO projectDAO;
    private final ProjectCriteriaSectionMapper projectCriteriaSectionMapper;


    @Autowired
    public GradeServiceImpl(ProjectDAO projectDAO,
                            ProjectCriteriaSectionMapper projectCriteriaSectionMapper) {
        this.projectDAO = projectDAO;
        this.projectCriteriaSectionMapper = projectCriteriaSectionMapper;
    }

    // TODO 11/20/2023: Move to EvaluationCardService
    /**
     * Creates a GradeDetailsDTO object that contains grade information for a specific project.
     * In addition to information about the criteria related to the project, there is also information about the
     * selected criteria for each Criteria Group. The selected criterion is calculated based on the points obtained by
     * the project in a specific Criteria Group.
     *
     * @param semester  - semester that the criteria are fetched for
     * @param projectId - project that the evaluation card is fetched for
     * @return Project grade details object that is distinguishable by semesters
     */
    @Override
    public GradeDetailsDTO findByProjectIdAndSemester(Semester semester, Long projectId) {
        GradeDetailsDTO projectGradeDetails = new GradeDetailsDTO();

        Project project = projectDAO.findById(projectId)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        projectGradeDetails.setId(projectId);
        projectGradeDetails.setProjectName(project.getName());
        projectGradeDetails.setSemester(semester);

        Double points = getPointsForSemester(project, semester);
        projectGradeDetails.setGrade(pointsToOverallPercent(points));

        List<CriteriaSection> sections = getCriteriaSectionsForSemester(project, semester);
        List<CriteriaSectionDTO> sectionDTOs = projectCriteriaSectionMapper.mapToDtoList(sections);


        List<Grade> projectGrades = project.getEvaluationCard().getGrades();
        Map<Long, Integer> projectPointsByGroupId = new HashMap<>();
        for (Grade projectGrade : projectGrades) {
            projectPointsByGroupId.put(projectGrade.getCriteriaGroup().getId(), projectGrade.getPoints());
        }

        sectionDTOs.forEach(section -> section.getCriteriaGroups().forEach(group ->
                group.setSelectedCriterion(CriterionCategory.getByPointsReceived(projectPointsByGroupId.get(group.getId())))
        ));

        projectGradeDetails.setSections(sectionDTOs);

        return projectGradeDetails;
    }

    /**
     * Returns points for semester of the specific project.
     */
    private Double getPointsForSemester(Project project, Semester semester) {
        return switch (semester) {
            case SEMESTER_I -> project.getEvaluationCard().getTotalPointsFirstSemester();
            case SEMESTER_II -> project.getEvaluationCard().getTotalPointsSecondSemester();
        };
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
     * Returns list of criteria sections which are related to the project in chosen semester
     */
    private List<CriteriaSection> getCriteriaSectionsForSemester(Project project, Semester semester) {
        return switch (semester) {
            case SEMESTER_I ->
                    project.getEvaluationCard().getEvaluationCardTemplate().getCriteriaSectionsFirstSemester();
            case SEMESTER_II ->
                    project.getEvaluationCard().getEvaluationCardTemplate().getCriteriaSectionsSecondSemester();
        };
    }

    @Override
    @Transactional
    public void updateSingleGrade(Grade grade, CriterionCategory newSelectedCriterionCategory) {
        Integer criterionPoints = CriterionCategory.getPoints(newSelectedCriterionCategory);
        CriteriaGroup gradeCriteriaGroup = grade.getCriteriaGroup();
        Optional<Criterion> criterion = gradeCriteriaGroup.getCriteria().stream()
                .filter(c -> c.getCriterionCategory().equals(newSelectedCriterionCategory))
                .findFirst();
        Double groupWeight = grade.getCriteriaGroup().getGradeWeight();

        if (criterion.isPresent()) {
            grade.setPoints(criterionPoints);
            grade.setPointsWithWeight(criterionPoints * groupWeight);
            grade.setDisqualifying(criterion.get().isDisqualifying());
        } else {
            grade.setPointsWithWeight(null);
            grade.setPoints(null);
            grade.setDisqualifying(false);
        }
    }

}
