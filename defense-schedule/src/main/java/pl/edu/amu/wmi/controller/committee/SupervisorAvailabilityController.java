package pl.edu.amu.wmi.controller.committee;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    @PutMapping("/supervisor")
    public ResponseEntity<Void> putSupervisorAvailability( //todo convert to map
            @RequestHeader("study-year") String studyYear,
            @Valid @RequestBody Map<String,SupervisorDefenseAssignmentDTO> supervisorDefenseAssignment) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        supervisorAvailabilityService.putSupervisorAvailability(studyYear, userDetails.getUsername(), supervisorDefenseAssignment);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Secured({"SUPERVISOR"})
    @GetMapping("/supervisor")
    public ResponseEntity<Map<String, Map<String, SupervisorDefenseAssignmentDTO>>> getSupervisorAvailability(
            @RequestHeader("study-year") String studyYear) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok()
                .body(supervisorAvailabilityService.getSupervisorAvailabilitySurvey(userDetails.getUsername(), studyYear));
    }

}
