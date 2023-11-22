package pl.edu.amu.wmi.service.project;

import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;

import java.util.List;

public interface ProjectService {

    /**
     * Finds all projects for selected study year
     * Projects are sorted based on the following rules: for student as a first position the confirmed project is displayed,
     * then all assigned projects and finally the other ones. For supervisor: firstly accepted projects, then assigned
     * and finally the rest of them.
     * Some project information are displayed only to users who are assigned to the projects (this restriction includes:
     * criteriaMet, pointsFirstSemester, pointsSecondSemester and externalLinks). These restrictions do not affect the user
     * with coordinator role.
     *
     * @param studyYear       study year that projects are fetched for
     * @param userIndexNumber index number of the user
     * @return list of {@link ProjectDTO} objects
     */
    List<ProjectDTO> findAllWithSortingAndRestrictions(String studyYear, String userIndexNumber);

    ProjectDetailsDTO findByIdWithRestrictions(String studyYear, String userIndexNumber, Long id);

    ProjectDetailsDTO saveProject(ProjectDetailsDTO project, String studyYear, String userIndexNumber);

    ProjectDetailsDTO updateProject(String studyYear, String userIndexNumber, Long projectId, ProjectDetailsDTO projectDTO);

    ProjectDetailsDTO updateProjectAdmin(Long projectId, String studentIndex);

    ProjectDetailsDTO acceptProject(String studyYear, String userIndexNumber, Long projectId);

    ProjectDetailsDTO unAcceptProject(String studyYear, String userIndexNumber, Long projectId);

    void delete(Long projectId, String userIndexNumber) throws ProjectManagementException;
}
