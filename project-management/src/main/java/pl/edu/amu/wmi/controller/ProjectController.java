package pl.edu.amu.wmi.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDataDTO;
import pl.edu.amu.wmi.model.grade.EvaluationCardDetails;
import pl.edu.amu.wmi.model.grade.SingleGroupGradeUpdateDTO;
import pl.edu.amu.wmi.model.grade.UpdatedGradeDTO;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;
import pl.edu.amu.wmi.model.project.SupervisorAvailabilityDTO;
import pl.edu.amu.wmi.service.externallink.ExternalLinkService;
import pl.edu.amu.wmi.service.grade.EvaluationCardPermissionService;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;
import pl.edu.amu.wmi.service.project.ProjectService;
import pl.edu.amu.wmi.service.project.SupervisorProjectService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;

    private final ExternalLinkService externalLinkService;

    private final SupervisorProjectService supervisorProjectService;

    private final EvaluationCardService evaluationCardService;

    private final EvaluationCardPermissionService evaluationCardPermissionService;

    // TODO: 11/23/2023 remove project dao from controller after tests
    private final ProjectDAO projectDAO;

    @Autowired
    public ProjectController(ProjectService projectService,
                             ExternalLinkService externalLinkService,
                             SupervisorProjectService supervisorProjectService,
                             EvaluationCardService evaluationCardService,
                             EvaluationCardPermissionService evaluationCardPermissionService, ProjectDAO projectDAO) {
        this.projectService = projectService;
        this.externalLinkService = externalLinkService;
        this.supervisorProjectService = supervisorProjectService;
        this.evaluationCardService = evaluationCardService;
        this.evaluationCardPermissionService = evaluationCardPermissionService;
        this.projectDAO = projectDAO;
    }

    @GetMapping("")
    public ResponseEntity<List<ProjectDTO>> getProjects(
            @RequestHeader("study-year") String studyYear) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok()
                .body(projectService.findAllWithSortingAndRestrictions(studyYear, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailsDTO> getProjectById(
            @PathVariable Long id,
            @RequestHeader("study-year") String studyYear) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok()
                .body(projectService.findByIdWithRestrictions(studyYear, userDetails.getUsername(), id));
    }

    @Secured({"PROJECT_ADMIN", "COORDINATOR"})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectById(@PathVariable Long id) throws ProjectManagementException {
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

    @GetMapping("/{projectId}/evaluation-card")
    public ResponseEntity<Map<Semester, Map<EvaluationPhase, EvaluationCardDetails>>> getGradeDetailsByProjectId(@RequestHeader("study-year") String studyYear, @PathVariable Long projectId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (evaluationCardPermissionService.isUserAllowedToSeeEvaluationDetails(studyYear, userDetails.getUsername())) {
            // TODO: 11/23/2023 implement response with error
        }
        return ResponseEntity.ok()
                .body(evaluationCardService.findEvaluationCards(projectId, studyYear, userDetails.getUsername()));
    }

    @Secured({"SUPERVISOR", "COORDINATOR"})
    @PutMapping("/{projectId}/evaluation-card/{evaluationCardId}")
    public ResponseEntity<UpdatedGradeDTO> updateEvaluationCardGrade(@PathVariable Long evaluationCardId, @RequestBody SingleGroupGradeUpdateDTO singleGroupGradeUpdate) {
        return ResponseEntity.ok()
                .body(evaluationCardService.updateEvaluationCard(evaluationCardId, singleGroupGradeUpdate));
    }

    // TODO: 11/22/2023 remove this endpoint (only for tests)
    @GetMapping("/{projectId}/evaluationCard/create")
    public ResponseEntity<Void> createEvaluationCard(@RequestHeader("study-year") String studyYear, @PathVariable Long projectId,
                                                     @RequestParam Semester semester,
                                                     @RequestParam EvaluationPhase evaluationPhase,
                                                     @RequestParam EvaluationStatus evaluationStatus) {
        Project project = projectDAO.findById(projectId).orElse(null);
        evaluationCardService.createEvaluationCard(project, studyYear, semester, evaluationPhase, evaluationStatus);
        return ResponseEntity.ok().build();
    }
}
