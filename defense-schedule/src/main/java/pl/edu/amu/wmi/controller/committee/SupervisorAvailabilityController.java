package pl.edu.amu.wmi.controller.committee;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.committee.SupervisorAvailabilityService;

import java.util.Map;


@RestController
@RequestMapping("/schedule/availability")
public class SupervisorAvailabilityController {

    private final SupervisorAvailabilityService supervisorAvailabilityService;

    @Autowired
    public SupervisorAvailabilityController(SupervisorAvailabilityService supervisorAvailabilityService) {
        this.supervisorAvailabilityService = supervisorAvailabilityService;
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

}
