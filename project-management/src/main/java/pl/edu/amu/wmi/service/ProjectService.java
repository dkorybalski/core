package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.ProjectDTO;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;

import java.util.List;

public interface ProjectService {

    List<ProjectDTO> findAll(String studyYear, String userIndexNumber);

    ProjectDetailsDTO saveProject(ProjectDetailsDTO project, String studyYear, String userIndexNumber);

}
