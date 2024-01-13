package pl.edu.amu.wmi.controller.scheduleconfig;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.model.scheduleconfig.DefensePhaseDTO;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleConfigDTO;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleModificationDTO;
import pl.edu.amu.wmi.service.notification.DefenseNotificationService;
import pl.edu.amu.wmi.service.scheduleconfig.DefenseScheduleConfigService;

@RestController
@Slf4j
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

    @Secured({"COORDINATOR", "SUPERVISOR"})
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

        Thread thread = new Thread(() -> {
            defenseNotificationService.notifyStudents(studyYear, DefensePhase.DEFENSE_PROJECT_REGISTRATION);
            log.info("Notifications about opening the defense registration process have been sent successfully");
        });
        thread.start();

        return ResponseEntity.ok(defensePhase);
    }

    @Secured({"COORDINATOR"})
    @PatchMapping("/registration/close")
    public ResponseEntity<DefensePhaseDTO> closeRegistrationForDefense(
            @RequestHeader("study-year") String studyYear) {
        DefensePhaseDTO defensePhase = defenseScheduleConfigService.closeRegistrationForDefense(studyYear);

        Thread thread = new Thread(() -> {
            defenseNotificationService.notifyStudents(studyYear, DefensePhase.DEFENSE_PROJECT);
            log.info("Notifications about closing the defense registration process have been sent successfully");
        });
        thread.start();

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
        defenseScheduleConfigService.archiveDefenseScheduleConfig(studyYear);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Secured({"COORDINATOR"})
    @PutMapping("/modify")
    public ResponseEntity<Void> modifyDefenseScheduleConfig(
            @RequestHeader("study-year") String studyYear,
            @Valid @RequestBody DefenseScheduleModificationDTO defenseScheduleModificationDTO) {
        defenseScheduleConfigService.modifyDefenseSchedule(studyYear, defenseScheduleModificationDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
