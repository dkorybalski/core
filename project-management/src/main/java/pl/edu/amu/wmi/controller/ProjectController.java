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
import pl.edu.amu.wmi.model.grade.EvaluationCardDetailsDTO;
import pl.edu.amu.wmi.model.grade.SingleGroupGradeUpdateDTO;
import pl.edu.amu.wmi.model.grade.UpdatedGradeDTO;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;
import pl.edu.amu.wmi.model.project.SupervisorAvailabilityDTO;
import pl.edu.amu.wmi.service.externallink.ExternalLinkService;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;
import pl.edu.amu.wmi.service.permission.PermissionService;
import pl.edu.amu.wmi.service.project.ProjectMemberService;
import pl.edu.amu.wmi.service.project.ProjectService;
import pl.edu.amu.wmi.service.project.SupervisorProjectService;

import java.util.Collections;
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

    private final PermissionService permissionService;

    private final ProjectMemberService projectMemberService;

    // TODO: 11/23/2023 remove project dao from controller after tests
    private final ProjectDAO projectDAO;

    @Autowired
    public ProjectController(ProjectService projectService,
                             ExternalLinkService externalLinkService,
                             SupervisorProjectService supervisorProjectService,
                             EvaluationCardService evaluationCardService,
                             PermissionService permissionService,
                             ProjectMemberService projectMemberService,
                             ProjectDAO projectDAO) {
        this.projectService = projectService;
        this.externalLinkService = externalLinkService;
        this.supervisorProjectService = supervisorProjectService;
        this.evaluationCardService = evaluationCardService;
        this.permissionService = permissionService;
        this.projectMemberService = projectMemberService;
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

    @Secured({"PROJECT_ADMIN", "COORDINATOR"})
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDetailsDTO> updateProject(
            @RequestHeader("study-year") String studyYear,
            @PathVariable Long id,
            @Valid @RequestBody ProjectDetailsDTO project) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok()
                .body(projectService.updateProject(studyYear, userDetails.getUsername(), id, project));
    }

    @GetMapping("/external-link/column-header")
    public ResponseEntity<Set<String>> getExternalLinkDefinitionColumnHeaders(@RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(externalLinkService.findDefinitionHeadersByStudyYear(studyYear));
    }

    @Secured({"STUDENT", "COORDINATOR"})
    @PostMapping("")
    public ResponseEntity<ProjectDetailsDTO> createProject(
            @RequestHeader("study-year") String studyYear,
            @RequestHeader("index-number") String userIndexNumber,
            @Valid @RequestBody ProjectDetailsDTO project) {
        String supervisorIndexNumber = project.getSupervisor().getIndexNumber();
        if (projectMemberService.isUserRoleCoordinator(userIndexNumber)) {
            if (!supervisorProjectService.isSupervisorAvailable(studyYear, supervisorIndexNumber)) {
                return ResponseEntity.status(409).build();
            }
            ProjectDetailsDTO projectDetailsDTO = projectService.saveProject(project, studyYear, userIndexNumber);
            projectService.acceptProjectByAllStudents(projectDetailsDTO.getId());
            projectService.acceptProjectBySingleUser(supervisorIndexNumber, projectDetailsDTO.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(projectDetailsDTO);
        } else {
            ProjectDetailsDTO projectDetailsDTO = projectService.saveProject(project, studyYear, userIndexNumber);
            projectService.acceptProjectBySingleUser(project.getAdmin(), projectDetailsDTO.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(projectDetailsDTO);
        }
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
                .body(projectService.acceptProjectBySingleUser(userIndexNumber, projectId));
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
    public ResponseEntity<Map<Semester, Map<EvaluationPhase, EvaluationCardDetailsDTO>>> getGradeDetailsByProjectId(@RequestHeader("study-year") String studyYear, @PathVariable Long projectId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!permissionService.isUserAllowedToSeeProjectDetails(studyYear, userDetails.getUsername(), projectId)) {
            return ResponseEntity.ok().body(Collections.emptyMap());
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

    @Secured({"COORDINATOR"})
    @PatchMapping("/{projectId}/evaluation-card/{evaluationCardId}/publish")
    public ResponseEntity<Void> publishEvaluationCard(@PathVariable Long evaluationCardId) {
        evaluationCardService.publishEvaluationCard(evaluationCardId);
        return ResponseEntity.ok().build();
    }

    @Secured({"COORDINATOR"})
    @PatchMapping("/{projectId}/evaluation-card/publish")
    public ResponseEntity<Void> publishEvaluationCards(@RequestHeader("study-year") String studyYear) {
        evaluationCardService.publishEvaluationCards(studyYear);
        return ResponseEntity.ok().build();
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
