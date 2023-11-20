package pl.edu.amu.wmi.service.project;

import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;

import java.util.List;

public interface ProjectService {

    List<ProjectDTO> findAllWithSorting(String studyYear, String userIndexNumber);

    ProjectDetailsDTO findById(Long id);

    ProjectDetailsDTO saveProject(ProjectDetailsDTO project, String studyYear, String userIndexNumber);

    ProjectDetailsDTO updateProject(String studyYear, String userIndexNumber, Long projectId, ProjectDetailsDTO projectDTO);

    ProjectDetailsDTO updateProjectAdmin(Long projectId, String studentIndex);

    ProjectDetailsDTO acceptProject(String studyYear, String userIndexNumber, Long projectId);

    ProjectDetailsDTO unAcceptProject(String studyYear, String userIndexNumber, Long projectId);

    void delete(Long projectId, String userIndexNumber) throws ProjectManagementException;
}
