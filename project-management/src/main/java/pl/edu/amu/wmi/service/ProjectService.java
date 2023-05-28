package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.ProjectDetailsDTO;

import java.util.List;

public interface ProjectService {

    List<ProjectDetailsDTO> findAll();

    ProjectDetailsDTO saveProject(ProjectDetailsDTO project, String studyYear, String userIndexNumber);
}
