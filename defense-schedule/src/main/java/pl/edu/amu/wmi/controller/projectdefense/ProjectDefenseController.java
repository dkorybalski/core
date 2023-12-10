package pl.edu.amu.wmi.controller.projectdefense;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefensePatchDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectNameDTO;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedule/defense")
public class ProjectDefenseController {

    private final ProjectDefenseService projectDefenseService;

    public ProjectDefenseController(ProjectDefenseService projectDefenseService) {
        this.projectDefenseService = projectDefenseService;
    }

    @GetMapping("")
    public ResponseEntity<Map<String, List<ProjectDefenseDTO>>> getProjectDefenses(@RequestHeader("study-year") String studyYear) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok()
                .body(projectDefenseService.getProjectDefenses(studyYear, userDetails.getUsername()));
    }

    @Secured({"PROJECT_ADMIN", "COORDINATOR"})
    @PatchMapping("/{projectDefenseId}")
    public ResponseEntity<Void> assignProjectToProjectDefense(
            @RequestHeader("study-year") String studyYear,
            @PathVariable Long projectDefenseId,
            @RequestBody ProjectDefensePatchDTO projectDefensePatchDTO) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        projectDefenseService.assignProjectToProjectDefense(studyYear, userDetails.getUsername(), projectDefenseId, projectDefensePatchDTO);
        return ResponseEntity.ok().build();
    }

    @Secured({"COORDINATOR"})
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectNameDTO>> getProjectNames(@RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(projectDefenseService.getProjectNames(studyYear));
    }

}
