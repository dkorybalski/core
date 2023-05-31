package pl.edu.amu.wmi.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.ExternalLinkDataDTO;
import pl.edu.amu.wmi.model.ProjectDTO;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;
import pl.edu.amu.wmi.service.ExternalLinkService;
import pl.edu.amu.wmi.service.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;

    private final ExternalLinkService externalLinkService;

    @Autowired
    public ProjectController(ProjectService projectService, ExternalLinkService externalLinkService) {
        this.projectService = projectService;
        this.externalLinkService = externalLinkService;
    }

    @GetMapping("")
    public ResponseEntity<List<ProjectDTO>> getProjects(
            @RequestHeader("study-year") String studyYear,
            @RequestHeader("user-index-number") String userIndexNumber) {
        return ResponseEntity.ok()
                .body(projectService.findAll(studyYear, userIndexNumber));
    }

    @PostMapping("")
    public ResponseEntity<ProjectDetailsDTO> createProject(
            @RequestHeader("study-year") String studyYear,
            @RequestHeader("user-index-number") String userIndexNumber,
            @Valid @RequestBody ProjectDetailsDTO project) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.saveProject(project, studyYear, userIndexNumber));
    }

    @GetMapping("/{projectId}/external-link")
    public ResponseEntity<ExternalLinkDataDTO> getExternalLinkDataByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok()
                .body(externalLinkService.findByProjectId(projectId));
    }

    @GetMapping("/external-link")
    public ResponseEntity<List<ExternalLinkDataDTO>> getExternalLinkData() {
        return ResponseEntity.ok()
                .body(externalLinkService.findAll());
    }

    @PostMapping("/external-link")
    public ResponseEntity<ExternalLinkDataDTO> createExternalLinkData(@RequestBody ExternalLinkDataDTO externalLinkData) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(externalLinkService.saveExternalLinkData(externalLinkData));
    }

    @PutMapping("/{projectId}/external-link")
    public ResponseEntity<ExternalLinkDataDTO> updateExternalLinkData(@Valid @RequestBody ExternalLinkDataDTO externalLinkData) {
        return ResponseEntity.ok()
                .body(externalLinkService.updateExternalLinkData(externalLinkData));
    }
}
