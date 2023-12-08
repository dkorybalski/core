package pl.edu.amu.wmi.controller.projectdefense;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;
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

}
