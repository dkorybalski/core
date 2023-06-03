package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.ProjectDTO;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;

import java.util.List;

public interface ProjectService {

    List<ProjectDTO> findAll(String studyYear, String userIndexNumber);

    ProjectDetailsDTO findById(Long id);

    ProjectDetailsDTO saveProject(ProjectDetailsDTO project, String studyYear, String userIndexNumber);

    ProjectDetailsDTO updateProjectAdmin(Long projectId, String studentIndex);

    ProjectDetailsDTO acceptProject(String studyYear, String userIndexNumber, Long projectId);

}
