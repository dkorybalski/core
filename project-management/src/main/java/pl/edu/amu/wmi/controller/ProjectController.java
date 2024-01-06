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
import pl.edu.amu.wmi.model.grade.EvaluationCardsDTO;
import pl.edu.amu.wmi.model.grade.SingleGroupGradeUpdateDTO;
import pl.edu.amu.wmi.model.grade.UpdatedGradeDTO;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;
import pl.edu.amu.wmi.model.project.SupervisorAvailabilityDTO;
import pl.edu.amu.wmi.service.PermissionService;
import pl.edu.amu.wmi.service.ProjectMemberService;
import pl.edu.amu.wmi.service.externallink.ExternalLinkService;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;
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
            @Valid @RequestBody ProjectDetailsDTO project) {
        String supervisorIndexNumber = project.getSupervisor().getIndexNumber();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (projectMemberService.isUserRoleCoordinator(userDetails.getUsername())) {
            if (!supervisorProjectService.isSupervisorAvailable(studyYear, supervisorIndexNumber)) {
                return ResponseEntity.status(409).build();
            }
            ProjectDetailsDTO projectDetailsDTO = projectService.saveProject(project, studyYear, userDetails.getUsername());
            projectService.acceptProjectByAllStudents(Long.valueOf(projectDetailsDTO.getId()));
            projectService.acceptProjectBySingleUser(supervisorIndexNumber, Long.valueOf(projectDetailsDTO.getId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(projectDetailsDTO);
        } else {
            ProjectDetailsDTO projectDetailsDTO = projectService.saveProject(project, studyYear, userDetails.getUsername());
            projectService.acceptProjectBySingleUser(project.getAdmin(), Long.valueOf(projectDetailsDTO.getId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(projectDetailsDTO);
        }
    }

    @Secured({"PROJECT_ADMIN", "COORDINATOR"})
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

    @Secured({"COORDINATOR"})
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
    @PutMapping("/{projectId}/evaluation-card/publish")
    public ResponseEntity<EvaluationCardsDTO> publishEvaluationCard(@RequestHeader("study-year") String studyYear, @PathVariable Long projectId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        evaluationCardService.publishEvaluationCard(projectId);
        return ResponseEntity.ok().body(prepareEvaluationCardsDTO(projectId, studyYear, userDetails.getUsername(), EvaluationStatus.PUBLISHED));
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/evaluation-card/publish")
    public ResponseEntity<Void> publishEvaluationCards(@RequestHeader("study-year") String studyYear) {
        evaluationCardService.publishEvaluationCards(studyYear);
        return ResponseEntity.ok().build();
    }

    // TODO: 11/22/2023 remove this endpoint (only for tests)
    @Secured({"COORDINATOR"})
    @GetMapping("/{projectId}/evaluationCard/create")
    public ResponseEntity<Void> createEvaluationCard(@RequestHeader("study-year") String studyYear, @PathVariable Long projectId,
                                                     @RequestParam Semester semester,
                                                     @RequestParam EvaluationPhase evaluationPhase,
                                                     @RequestParam EvaluationStatus evaluationStatus,
                                                     @RequestParam boolean isActive) {
        Project project = projectDAO.findById(projectId).orElse(null);
        evaluationCardService.createEvaluationCard(project, studyYear, semester, evaluationPhase, evaluationStatus, isActive);
        return ResponseEntity.ok().build();
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/{projectId}/evaluation-card/freeze")
    public ResponseEntity<EvaluationCardsDTO> freezeEvaluationCard(@RequestHeader("study-year") String studyYear,
                                                                                                              @PathVariable Long projectId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        evaluationCardService.freezeEvaluationCard(projectId);
        return ResponseEntity.ok()
                .body(prepareEvaluationCardsDTO(projectId, studyYear, userDetails.getUsername(), EvaluationStatus.FROZEN));
    }

    private EvaluationCardsDTO prepareEvaluationCardsDTO(Long projectId, String studyYear, String indexNumber, EvaluationStatus status) {
        return new EvaluationCardsDTO(
                evaluationCardService.findEvaluationCards(projectId, studyYear, indexNumber),
                status.label
        );
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/{projectId}/evaluation-card/retake")
    public ResponseEntity<EvaluationCardsDTO> retakeEvaluationCard(@RequestHeader("study-year") String studyYear,
                                                                                                              @PathVariable Long projectId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        evaluationCardService.retakeEvaluationCard(projectId);
        return ResponseEntity.ok()
                .body(prepareEvaluationCardsDTO(projectId, studyYear, userDetails.getUsername(), EvaluationStatus.RETAKE));
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/evaluation-card/activate-second-semester")
    public ResponseEntity<Void> activateEvaluationCardsForSecondSemester
            (@RequestHeader("study-year") String studyYear) {
        evaluationCardService.activateEvaluationCardsForSecondSemester(studyYear);
        return ResponseEntity.ok().build();
    }
}
