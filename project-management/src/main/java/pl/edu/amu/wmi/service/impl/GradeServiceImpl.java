package pl.edu.amu.wmi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.CriteriaSection;
import pl.edu.amu.wmi.entity.Grade;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.CriterionCategory;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.exception.ProjectManagementException;
import pl.edu.amu.wmi.mapper.PointsMapper;
import pl.edu.amu.wmi.mapper.ProjectCriteriaSectionMapper;
import pl.edu.amu.wmi.model.ProjectCriteriaSectionDTO;
import pl.edu.amu.wmi.model.ProjectGradeDetailsDTO;
import pl.edu.amu.wmi.service.GradeService;

import java.text.MessageFormat;
import java.util.List;

@Service
public class GradeServiceImpl implements GradeService {

    private final ProjectDAO projectDAO;
    private final ProjectCriteriaSectionMapper projectCriteriaSectionMapper;
    private final PointsMapper pointsMapper;


    @Autowired
    public GradeServiceImpl(ProjectDAO projectDAO, ProjectCriteriaSectionMapper projectCriteriaSectionMapper, PointsMapper pointsMapper) {
        this.projectDAO = projectDAO;
        this.projectCriteriaSectionMapper = projectCriteriaSectionMapper;
        this.pointsMapper = pointsMapper;
    }

    /**
     * Creates a ProjectGradeDetailsDTO object that contains grade information for a specific project.
     * In addition to information about the criteria related to the project, there is also information about the
     * selected criteria for each Criteria Group. The selected criterion is calculated based on the points obtained by
     * the project in a specific Criteria Group {@link #getSelectedCriterion(Project, String)}.
     *
     * @param semester - semester that the criteria are fetched for
     * @param projectId - project that the evaluation card is fetched for
     * @return Project grade details object that is distinguishable by semesters
     */
    @Override
    public ProjectGradeDetailsDTO findByProjectIdAndSemester(Semester semester, Long projectId) {
        ProjectGradeDetailsDTO projectGradeDetails = new ProjectGradeDetailsDTO();

        Project project = projectDAO.findById(projectId)
                .orElseThrow(() -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        projectGradeDetails.setId(projectId);
        projectGradeDetails.setProjectName(project.getName());
        projectGradeDetails.setSemester(semester);

        Double points = getPointsForSemester(project, semester);
        projectGradeDetails.setGrade(pointsMapper.mapPointsToPercent(points));

        List<CriteriaSection> sections = getCriteriaSectionsForSemester(project, semester);
        List<ProjectCriteriaSectionDTO> sectionDTOs = projectCriteriaSectionMapper.mapToDtoList(sections);
        setSelectedCriteria(project, sectionDTOs);
        projectGradeDetails.setSections(sectionDTOs);

        return projectGradeDetails;
    }

    /**
     * @return Points value which was received by the project in the chosen semester
     */
    private Double getPointsForSemester(Project project, Semester semester) {
        return switch (semester) {
            case SEMESTER_I -> project.getEvaluationCard().getTotalPointsFirstSemester();
            case SEMESTER_II -> project.getEvaluationCard().getTotalPointsSecondSemester();
        };
    }

    /**
     * @return List of criteria sections which are related to the project in chosen semester
     */
    private List<CriteriaSection> getCriteriaSectionsForSemester(Project project, Semester semester) {
        return switch (semester) {
            case SEMESTER_I -> project.getEvaluationCard().getEvaluationCardTemplate().getCriteriaSectionsFirstSemester();
            case SEMESTER_II -> project.getEvaluationCard().getEvaluationCardTemplate().getCriteriaSectionsSecondSemester();
        };
    }

    /**
     * Iterates through the ProjectCriteriaSectionDTO objects, and then in each section, iterates over the section's
     * ProjectCriteriaGroupDTO objects to set the selected criterion found by the
     * {@link #getSelectedCriterion(Project, String)} method, which returns the {@link CriterionCategory} enumeration.
     * This enumeration has an Integer field whose value is set in the ProjectCriteriaGroupDTO.
     *
     * @param project - project entity
     * @param sections - list of ProjectCriteriaSectionDTOs where selected criteria must be set
     */
    private void setSelectedCriteria(Project project, List<ProjectCriteriaSectionDTO> sections) {
        sections.forEach(section -> section.getCriteriaGroups()
                .forEach(criteriaGroup ->
                {
                    CriterionCategory criterionCategory = getSelectedCriterion(project, criteriaGroup.getName());
                    if (criterionCategory == null)
                        criteriaGroup.setSelectedCriterion(null);
                    else
                        criteriaGroup.setSelectedCriterion(criterionCategory.getValue());
                })
        );
    }

    /**
     * Finds a project's CriteriaGroup grade and, based on the project's grade in that group, finds an appropriate enumeration object.
     *
     * @param project - project entity
     * @param criteriaGroupName - name of the criteria group for which the criteria category must be set
     * @return {@link CriterionCategory} enum object.
     * If the project does not yet have a grade in the specified Criteria Group, the enumeration will not be found and
     * the method will return null.
     * Otherwise, the findByPointsReceived(points) method will return an enum based on the point values.
     */
    private CriterionCategory getSelectedCriterion(Project project, String criteriaGroupName) {
        Grade grade = project.getEvaluationCard().getGrades().stream()
                .filter(g -> g.getCriteriaGroup().getName().equals(criteriaGroupName))
                .findFirst()
                .orElse(null);
        Integer points = null;
        if (grade != null)
            points = grade.getPoints();
        return CriterionCategory.findByPointsReceived(points);
    }

}
