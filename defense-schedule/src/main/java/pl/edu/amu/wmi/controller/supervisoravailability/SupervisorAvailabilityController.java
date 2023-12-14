package pl.edu.amu.wmi.controller.supervisoravailability;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorStatisticsDTO;
import pl.edu.amu.wmi.service.supervisoravailability.CommitteeService;
import pl.edu.amu.wmi.service.supervisoravailability.SupervisorAvailabilityService;
import pl.edu.amu.wmi.service.supervisoravailability.SupervisorStatisticsService;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/schedule/availability")
public class SupervisorAvailabilityController {

    private final SupervisorAvailabilityService supervisorAvailabilityService;
    private final SupervisorStatisticsService supervisorStatisticsService;
    private final CommitteeService committeeService;

    @Autowired
    public SupervisorAvailabilityController(SupervisorAvailabilityService supervisorAvailabilityService, SupervisorStatisticsService supervisorStatisticsService, CommitteeService committeeService) {
        this.supervisorAvailabilityService = supervisorAvailabilityService;
        this.supervisorStatisticsService = supervisorStatisticsService;
        this.committeeService = committeeService;
    }

    @Secured({"COORDINATOR", "SUPERVISOR"})
    @PutMapping("/supervisor/{supervisorId}")
    public ResponseEntity<Void> putSupervisorAvailability( //todo convert to map
            @RequestHeader("study-year") String studyYear,
            @PathVariable Long supervisorId,
            @Valid @RequestBody Map<String,SupervisorDefenseAssignmentDTO> supervisorDefenseAssignment) {
        supervisorAvailabilityService.putSupervisorAvailability(studyYear, supervisorId, supervisorDefenseAssignment);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Secured({"SUPERVISOR"})
    @GetMapping("/supervisor/{supervisorId}")
    public ResponseEntity<Map<String, Map<String, SupervisorDefenseAssignmentDTO>>> getSupervisorAvailability(
            @RequestHeader("study-year") String studyYear,
            @PathVariable Long supervisorId) {
        return ResponseEntity.ok()
                .body(supervisorAvailabilityService.getSupervisorAvailabilitySurvey(supervisorId));
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
    @GetMapping("/statistics")
    public ResponseEntity<List<SupervisorStatisticsDTO>> getSupervisorStatistics(@RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok()
                .body(supervisorStatisticsService.getSupervisorStatistics(studyYear));
    }

}
