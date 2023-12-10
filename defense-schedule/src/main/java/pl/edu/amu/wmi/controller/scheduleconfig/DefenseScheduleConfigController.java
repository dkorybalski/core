package pl.edu.amu.wmi.controller.scheduleconfig;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.enumerations.DefensePhase;
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
    @PatchMapping("/registration/open")
    public ResponseEntity<Void> openRegistrationForDefense(
            @RequestHeader("study-year") String studyYear) {
        defenseScheduleConfigService.openRegistrationForDefense(studyYear);
        defenseNotificationService.notifyStudents(studyYear, DefensePhase.DEFENSE_PROJECT_REGISTRATION);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}