package pl.edu.amu.wmi.controller.committee;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.model.committee.ChairpersonAssignmentDTO;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.model.committee.SupervisorStatisticsDTO;
import pl.edu.amu.wmi.service.committee.CommitteeService;
import pl.edu.amu.wmi.service.committee.SupervisorAvailabilityService;
import pl.edu.amu.wmi.service.committee.SupervisorStatisticsService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schedule/committee")
public class CommitteeController {

    private final CommitteeService committeeService;

    private final SupervisorAvailabilityService supervisorAvailabilityService;

    private final SupervisorStatisticsService supervisorStatisticsService;

    public CommitteeController(CommitteeService committeeService, SupervisorAvailabilityService supervisorAvailabilityService, SupervisorStatisticsService supervisorStatisticsService) {
        this.committeeService = committeeService;
        this.supervisorAvailabilityService = supervisorAvailabilityService;
        this.supervisorStatisticsService = supervisorStatisticsService;
    }

    @Secured({"COORDINATOR"})
    @GetMapping("/supervisor")
    public ResponseEntity<Map<String, Map<String, Map<String, SupervisorDefenseAssignmentDTO>>>> getSupervisorsAvailability(
            @RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(supervisorAvailabilityService.getAggregatedSupervisorsAvailability(studyYear));
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/supervisor")
    public ResponseEntity<List<SupervisorStatisticsDTO>>updateCommittee(
            @RequestHeader("study-year") String studyYear,
            @RequestBody Map<String, SupervisorDefenseAssignmentDTO> supervisorDefenseAssignmentDTOMap) {
        committeeService.updateCommittee(studyYear, supervisorDefenseAssignmentDTOMap);
        return ResponseEntity.ok()
                .body(supervisorStatisticsService.getSupervisorStatistics(studyYear));
    }

    @Secured({"COORDINATOR"})
    @GetMapping("/chairperson")
    public ResponseEntity<Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>>> getChairpersonAssignments(
            @RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(committeeService.getAggregatedChairpersonAssignments(studyYear));
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/chairperson")
    public ResponseEntity<Void> updateChairpersonAssignment(
            @RequestHeader("study-year") String studyYear,
            @RequestBody ChairpersonAssignmentDTO chairpersonAssignmentDTO) {
        committeeService.updateChairpersonAssignment(chairpersonAssignmentDTO, studyYear);
        return ResponseEntity.ok().build();
    }

    @Secured({"COORDINATOR"})
    @GetMapping("/statistics")
    public ResponseEntity<List<SupervisorStatisticsDTO>> getSupervisorStatistics(@RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(supervisorStatisticsService.getSupervisorStatistics(studyYear));
    }

}
