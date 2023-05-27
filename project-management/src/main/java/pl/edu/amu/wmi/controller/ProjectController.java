package pl.edu.amu.wmi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.ProjectCreationRequestDTO;
import pl.edu.amu.wmi.model.ProjectCreationResponseDTO;
import pl.edu.amu.wmi.service.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/")
    public ResponseEntity<List<ProjectCreationRequestDTO>> getProjects() {
        return ResponseEntity.ok()
                .body(projectService.findAll());
    }

    @PostMapping("/")
    public ResponseEntity<ProjectCreationResponseDTO> createProject(@RequestBody ProjectCreationRequestDTO project) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.saveProject(project));
    }
}
