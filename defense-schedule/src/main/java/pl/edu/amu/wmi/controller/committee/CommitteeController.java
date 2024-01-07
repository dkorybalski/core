package pl.edu.amu.wmi.controller.committee;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.model.committee.ChairpersonAssignmentDTO;
import pl.edu.amu.wmi.model.committee.CommitteeAssignmentSummaryDTO;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.model.committee.SupervisorStatisticsDTO;
import pl.edu.amu.wmi.service.committee.CommitteeService;
import pl.edu.amu.wmi.service.committee.SupervisorAvailabilityService;
import pl.edu.amu.wmi.service.committee.SupervisorStatisticsService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedule/committee")
public class CommitteeController {

    private final CommitteeService committeeService;

    private final SupervisorAvailabilityService supervisorAvailabilityService;

    private final SupervisorStatisticsService supervisorStatisticsService;

    private final ProjectDefenseService projectDefenseService;

    public CommitteeController(CommitteeService committeeService, SupervisorAvailabilityService supervisorAvailabilityService, SupervisorStatisticsService supervisorStatisticsService, ProjectDefenseService projectDefenseService) {
        this.committeeService = committeeService;
        this.supervisorAvailabilityService = supervisorAvailabilityService;
        this.supervisorStatisticsService = supervisorStatisticsService;
        this.projectDefenseService = projectDefenseService;
    }

    @Secured({"COORDINATOR", "SUPERVISOR"})
    @GetMapping("/supervisor")
    public ResponseEntity<Map<String, Map<String, Map<String, SupervisorDefenseAssignmentDTO>>>> getSupervisorsAvailability(
            @RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(supervisorAvailabilityService.getAggregatedSupervisorsAvailability(studyYear));
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/supervisor")
    public ResponseEntity<CommitteeAssignmentSummaryDTO>updateCommittee(
            @RequestHeader("study-year") String studyYear,
            @RequestBody Map<String, SupervisorDefenseAssignmentDTO> supervisorDefenseAssignmentDTOMap) {
        committeeService.updateCommittee(studyYear, supervisorDefenseAssignmentDTOMap);
        return ResponseEntity.ok()
                .body(prepareCommitteeAssignmentSummary(studyYear));
    }

    @Secured({"COORDINATOR", "SUPERVISOR"})
    @GetMapping("/chairperson")
    public ResponseEntity<Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>>> getChairpersonAssignments(
            @RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(committeeService.getAggregatedChairpersonAssignments(studyYear));
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/chairperson")
    public ResponseEntity<CommitteeAssignmentSummaryDTO> updateChairpersonAssignment(
            @RequestHeader("study-year") String studyYear,
            @RequestBody ChairpersonAssignmentDTO chairpersonAssignmentDTO) {
        committeeService.updateChairpersonAssignment(chairpersonAssignmentDTO, studyYear);
        return ResponseEntity.ok(prepareCommitteeAssignmentSummary(studyYear));
    }

    @Secured({"COORDINATOR", "SUPERVISOR"})
    @GetMapping("/statistics")
    public ResponseEntity<List<SupervisorStatisticsDTO>> getSupervisorStatistics(@RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(supervisorStatisticsService.getSupervisorStatistics(studyYear));
    }

    private CommitteeAssignmentSummaryDTO prepareCommitteeAssignmentSummary(String studyYear) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new CommitteeAssignmentSummaryDTO(
                supervisorStatisticsService.getSupervisorStatistics(studyYear),
                projectDefenseService.getProjectDefenses(studyYear, userDetails.getUsername())
        );
    }

}
