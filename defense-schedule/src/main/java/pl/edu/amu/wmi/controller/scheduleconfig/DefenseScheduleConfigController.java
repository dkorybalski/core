package pl.edu.amu.wmi.controller.scheduleconfig;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.model.scheduleconfig.DefensePhaseDTO;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleConfigDTO;
import pl.edu.amu.wmi.service.notification.DefenseNotificationService;
import pl.edu.amu.wmi.service.scheduleconfig.DefenseScheduleConfigService;

@RestController
@RequestMapping("/schedule/config")
public class DefenseScheduleConfigController {

    private final DefenseScheduleConfigService defenseScheduleConfigService;
    private final DefenseNotificationService defenseNotificationService;

    @Autowired
    public DefenseScheduleConfigController(DefenseScheduleConfigService defenseScheduleConfigService, DefenseNotificationService defenseNotificationService) {
        this.defenseScheduleConfigService = defenseScheduleConfigService;
        this.defenseNotificationService = defenseNotificationService;
    }

    @Secured({"COORDINATOR"})
    @PostMapping("")
    public ResponseEntity<Void> createDefenseScheduleConfig(
            @RequestHeader("study-year") String studyYear,
            @Valid @RequestBody DefenseScheduleConfigDTO defenseScheduleConfig) {
        defenseScheduleConfigService.createDefenseScheduleConfig(studyYear, defenseScheduleConfig);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"COORDINATOR"})
    @GetMapping("/phase")
    public ResponseEntity<DefensePhaseDTO> getCurrentDefensePhase(
            @RequestHeader("study-year") String studyYear) {
        return ResponseEntity.ok(defenseScheduleConfigService.getCurrentDefensePhase(studyYear));
    }

    @Secured({"COORDINATOR"})
    @PatchMapping("/registration/open")
    public ResponseEntity<DefensePhaseDTO> openRegistrationForDefense(
            @RequestHeader("study-year") String studyYear) {
        DefensePhaseDTO defensePhase = defenseScheduleConfigService.openRegistrationForDefense(studyYear);
        // TODO: 12/10/2023 consider making this call asynchronous
        defenseNotificationService.notifyStudents(studyYear, DefensePhase.DEFENSE_PROJECT_REGISTRATION);
        return ResponseEntity.ok(defensePhase);
    }

    @Secured({"COORDINATOR"})
    @PatchMapping("/registration/close")
    public ResponseEntity<DefensePhaseDTO> closeRegistrationForDefense(
            @RequestHeader("study-year") String studyYear) {
        DefensePhaseDTO defensePhase = defenseScheduleConfigService.closeRegistrationForDefense(studyYear);
        // TODO: 12/10/2023 consider making this call asynchronous
        defenseNotificationService.notifyStudents(studyYear, DefensePhase.DEFENSE_PROJECT);
        return ResponseEntity.ok(defensePhase);
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/rebuild")
    public ResponseEntity<Void> rebuildDefenseScheduleConfig(
            @RequestHeader("study-year") String studyYear) {
        defenseScheduleConfigService.deleteActiveScheduleConfig(studyYear);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/archive")
    public ResponseEntity<Void> archiveDefenseScheduleConfig(
            @RequestHeader("study-year") String studyYear) {
        // TODO: 1/5/2024 finish implementation - all searches for defense related data havo to filter by active config
//        defenseScheduleConfigService.archiveDefenseScheduleConfig(studyYear);
//        return ResponseEntity.status(HttpStatus.OK).build();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

}
