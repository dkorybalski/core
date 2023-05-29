package pl.edu.amu.wmi.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;
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
    public ResponseEntity<List<ProjectDetailsDTO>> getProjects() {
        return ResponseEntity.ok()
                .body(projectService.findAll());
    }

    @PostMapping("/")
    public ResponseEntity<ProjectDetailsDTO> createProject(
            @RequestHeader("study-year") String studyYear,
            @RequestHeader("user-index-number") String userIndexNumber,
            @Valid @RequestBody ProjectDetailsDTO project) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.saveProject(project, studyYear, userIndexNumber));
    }
}
