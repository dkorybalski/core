package pl.edu.amu.wmi.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.*;
import pl.edu.amu.wmi.service.ExternalLinkService;
import pl.edu.amu.wmi.service.ProjectService;
import pl.edu.amu.wmi.service.SupervisorProjectService;

import java.util.List;
import java.util.Set;

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
                .body(projectService.findAllWithSorting(studyYear, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailsDTO> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok()
                .body(projectService.findById(id));
    }

    @Secured({"PROJECT_ADMIN", "COORDINATOR"})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectById(@PathVariable Long id) throws Exception {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        projectService.delete(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDetailsDTO> updateProject(
            @RequestHeader("study-year") String studyYear,
            @RequestHeader("index-number") String userIndexNumber,
            @PathVariable Long id,
            @Valid @RequestBody ProjectDetailsDTO project) {
        return ResponseEntity.ok()
                .body(projectService.updateProject(studyYear, userIndexNumber, id, project));
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
    public ResponseEntity<Set<ExternalLinkDTO>> getExternalLinksByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok()
                .body(externalLinkService.findByProjectId(projectId));
    }

    @GetMapping("/external-link")
    public ResponseEntity<List<ExternalLinkDataDTO>> getExternalLinkData() {
        return ResponseEntity.ok()
                .body(externalLinkService.findAll());
    }

    @GetMapping("/external-link/column-header")
    public ResponseEntity<Set<String>> getExternalLinkDefinitionColumnHeaders(@RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(externalLinkService.findDefinitionHeadersByStudyYear(studyYear));
    }

    @PutMapping("/{projectId}/external-link")
    public ResponseEntity<Set<ExternalLinkDTO>> updateExternalLinkData(
             @PathVariable Long projectId,
             @Valid @RequestBody Set<ExternalLinkDTO> externalLinks) {
        return ResponseEntity.ok()
                .body(externalLinkService.updateExternalLinks(projectId, externalLinks));
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

    @PatchMapping("/{projectId}/unaccept")
    public ResponseEntity<ProjectDetailsDTO> unAcceptProject(
            @RequestHeader("study-year") String studyYear,
            @RequestHeader("index-number") String userIndexNumber,
            @PathVariable Long projectId) {
        return ResponseEntity.ok()
                .body(projectService.unAcceptProject(studyYear, userIndexNumber, projectId));
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
