package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.ProjectDetailsDTO;

import java.util.List;

public interface ProjectService {

    ProjectDetailsDTO findById(Long id);

    List<ProjectDetailsDTO> findAll();

    ProjectDetailsDTO saveProject(ProjectDetailsDTO project, String studyYear, String userIndexNumber);
}
