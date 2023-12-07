package pl.edu.amu.wmi.controller.supervisoravailability;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.supervisoravailability.SupervisorAvailabilityService;


@RestController
@RequestMapping("/schedule/availability/supervisor")
public class SupervisorAvailabilityController {

    private final SupervisorAvailabilityService supervisorAvailabilityService;

    @Autowired
    public SupervisorAvailabilityController(SupervisorAvailabilityService supervisorAvailabilityService) {
        this.supervisorAvailabilityService = supervisorAvailabilityService;
    }

    @Secured({"COORDINATOR", "SUPERVISOR"})
    @PutMapping("/{supervisorId}")
    public ResponseEntity<Void> putSupervisorAvailability(
            @RequestHeader("study-year") String studyYear,
            @PathVariable Long supervisorId,
            @Valid @RequestBody SupervisorDefenseAssignmentDTO supervisorDefenseAssignment) {
        supervisorAvailabilityService.putSupervisorAvailability(studyYear, supervisorId, supervisorDefenseAssignment);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
