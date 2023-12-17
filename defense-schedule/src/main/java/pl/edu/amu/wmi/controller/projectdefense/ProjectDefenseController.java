package pl.edu.amu.wmi.controller.projectdefense;

import com.opencsv.exceptions.CsvException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefensePatchDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectNameDTO;
import pl.edu.amu.wmi.service.notification.DefenseNotificationService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseSummaryService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/schedule/defense")
public class ProjectDefenseController {

    private final ProjectDefenseService projectDefenseService;
    private final ProjectDefenseSummaryService projectDefenseSummaryService;
    private final DefenseNotificationService defenseNotificationService;

    public ProjectDefenseController(ProjectDefenseService projectDefenseService,
                                    ProjectDefenseSummaryService projectDefenseSummaryService,
                                    DefenseNotificationService defenseNotificationService) {
        this.projectDefenseService = projectDefenseService;
        this.projectDefenseSummaryService = projectDefenseSummaryService;
        this.defenseNotificationService = defenseNotificationService;
    }

    @GetMapping("")
    public ResponseEntity<List<ProjectDefenseDTO>> getProjectDefenses(@RequestHeader("study-year") String studyYear) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok()
                .body(projectDefenseService.getProjectDefenses(studyYear, userDetails.getUsername()));
    }

    @Secured({"PROJECT_ADMIN"})
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
    @PatchMapping("")
    public ResponseEntity<Void> updateProjectDefenses(
            @RequestHeader("study-year") String studyYear,
            @RequestBody List<ProjectDefenseDTO> projectDefenseDTOs) {
        Set<Project> updatedProjectDefenses = projectDefenseService.assignProjectsToProjectDefenses(projectDefenseDTOs);
        List<Student> studentsToNotify = projectDefenseService.getStudentsFromProjectDefenses(updatedProjectDefenses);
        defenseNotificationService.notifyStudentsAboutProjectDefenseAssignment(studentsToNotify);
        return ResponseEntity.ok().build();
    }

    @Secured({"COORDINATOR"})
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectNameDTO>> getProjectNames(@RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(projectDefenseService.getProjectNames(studyYear));
    }

    @Secured({"COORDINATOR"})
    @GetMapping("/summary")
    public void exportDefenseScheduleSummary(@RequestHeader("study-year") String studyYear, HttpServletResponse servletResponse) throws IOException, CsvException {
        servletResponse.setContentType("text/csv");
        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.addHeader("Content-Disposition", "attachment; filename=\"schedule.csv\"");

        projectDefenseSummaryService.exportDefenseScheduleSummaryData(servletResponse.getWriter(), studyYear);
    }

}
