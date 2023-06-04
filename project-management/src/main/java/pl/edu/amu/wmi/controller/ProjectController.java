package pl.edu.amu.wmi.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.ExternalLinkDataDTO;
import pl.edu.amu.wmi.model.ProjectDTO;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;
import pl.edu.amu.wmi.model.SupervisorAvailabilityDTO;
import pl.edu.amu.wmi.service.ExternalLinkService;
import pl.edu.amu.wmi.service.ProjectService;
import pl.edu.amu.wmi.service.SupervisorProjectService;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;

    private final ExternalLinkService externalLinkService;

    private final SupervisorProjectService supervisorProjectService;

    @Autowired
    public ProjectController(ProjectService projectService, ExternalLinkService externalLinkService, SupervisorProjectService supervisorProjectService) {
        this.projectService = projectService;
        this.externalLinkService = externalLinkService;
        this.supervisorProjectService = supervisorProjectService;
    }

    @GetMapping("")
    public ResponseEntity<List<ProjectDTO>> getProjects(
            @RequestHeader("study-year") String studyYear) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok()
                .body(projectService.findAll(studyYear, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailsDTO> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok()
                .body(projectService.findById(id));
    }

    @PostMapping("")
    public ResponseEntity<ProjectDetailsDTO> createProject(
            @RequestHeader("study-year") String studyYear,
            @RequestHeader("index-number") String userIndexNumber,
            @Valid @RequestBody ProjectDetailsDTO project) {

        ProjectDetailsDTO projectDetailsDTO = projectService.saveProject(project, studyYear, userIndexNumber);
        projectService.acceptProject(studyYear, userIndexNumber, projectDetailsDTO.getId());
        projectService.updateProjectAdmin(projectDetailsDTO.getId(), userIndexNumber);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectDetailsDTO);
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

    @PatchMapping("/{projectId}/admin-change/{studentIndex}")
    public ResponseEntity<ProjectDetailsDTO> updateProjectAdmin(
            @PathVariable Long projectId,
            @PathVariable String studentIndex) {
        return ResponseEntity.ok()
                .body(projectService.updateProjectAdmin(projectId, studentIndex));
    }

    @PatchMapping("/{projectId}/accept")
    public ResponseEntity<ProjectDetailsDTO> acceptProject(
            @RequestHeader("study-year") String studyYear,
            @RequestHeader("index-number") String userIndexNumber,
            @PathVariable Long projectId) {
        return ResponseEntity.ok()
                .body(projectService.acceptProject(studyYear, userIndexNumber, projectId));
    }

    @GetMapping("/supervisor/availability")
    public ResponseEntity<List<SupervisorAvailabilityDTO>> getSupervisorsAvailability(@RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(supervisorProjectService.getSupervisorsAvailability(studyYear));
    }

    @PutMapping("/supervisor/availability")
    public ResponseEntity<List<SupervisorAvailabilityDTO>> updateSupervisorsAvailability(@RequestHeader("study-year") String studyYear, @RequestBody List<SupervisorAvailabilityDTO> supervisorAvailabilityList) {
        return ResponseEntity.ok()
                .body(supervisorProjectService.updateSupervisorsAvailability(studyYear, supervisorAvailabilityList));
    }
}
