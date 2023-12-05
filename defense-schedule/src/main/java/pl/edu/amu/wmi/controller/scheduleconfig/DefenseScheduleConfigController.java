package pl.edu.amu.wmi.controller.scheduleconfig;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleConfigDTO;
import pl.edu.amu.wmi.service.scheduleconfig.DefenseScheduleConfigService;

@RestController
@RequestMapping("/schedule/config")
public class DefenseScheduleConfigController {

    private final DefenseScheduleConfigService defenseScheduleConfigService;

    @Autowired
    public DefenseScheduleConfigController(DefenseScheduleConfigService defenseScheduleConfigService) {
        this.defenseScheduleConfigService = defenseScheduleConfigService;
    }

    @Secured({"COORDINATOR"})
    @PostMapping("")
    public ResponseEntity<Void> createDefenseScheduleConfig(
            @RequestHeader("study-year") String studyYear,
            @Valid @RequestBody DefenseScheduleConfigDTO defenseScheduleConfig) {
        defenseScheduleConfigService.createDefenseScheduleConfig(studyYear, defenseScheduleConfig);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}