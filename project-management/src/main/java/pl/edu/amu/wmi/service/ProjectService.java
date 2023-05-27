package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.ProjectCreationRequestDTO;
import pl.edu.amu.wmi.model.ProjectCreationResponseDTO;

import java.util.List;

public interface ProjectService {

    List<ProjectCreationRequestDTO> findAll();

    ProjectCreationResponseDTO saveProject(ProjectCreationRequestDTO project);
}
