package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.ProjectDTO;

import java.util.List;

public interface ProjectService {

    List<ProjectDTO> findAll();

    ProjectDTO saveProject(ProjectDTO project);

}
