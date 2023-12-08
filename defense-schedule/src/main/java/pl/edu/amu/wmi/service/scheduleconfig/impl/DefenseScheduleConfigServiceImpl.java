package pl.edu.amu.wmi.service.scheduleconfig.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.DefenseScheduleConfigDAO;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.mapper.scheduleconfig.DefenseScheduleConfigMapper;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleConfigDTO;
import pl.edu.amu.wmi.service.defensetimeslot.DefenseTimeSlotService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;
import pl.edu.amu.wmi.service.scheduleconfig.DefenseScheduleConfigService;
import pl.edu.amu.wmi.service.supervisordefense.SupervisorDefenseAssignmentService;

import java.util.Objects;


@Slf4j
@Service
public class DefenseScheduleConfigServiceImpl implements DefenseScheduleConfigService {

    private final DefenseTimeSlotService defenseTimeSlotService;
    private final ProjectDefenseService projectDefenseService;
    private final SupervisorDefenseAssignmentService supervisorDefenseAssignmentService;
    private final DefenseScheduleConfigDAO defenseScheduleConfigDAO;
    private final DefenseScheduleConfigMapper defenseScheduleConfigMapper;


    @Autowired
    public DefenseScheduleConfigServiceImpl(DefenseTimeSlotService defenseTimeSlotService,
                                            ProjectDefenseService projectDefenseService, SupervisorDefenseAssignmentService supervisorDefenseAssignmentService,
                                            DefenseScheduleConfigDAO defenseScheduleConfigDAO,
                                            DefenseScheduleConfigMapper defenseScheduleConfigMapper) {
        this.defenseTimeSlotService = defenseTimeSlotService;
        this.projectDefenseService = projectDefenseService;
        this.supervisorDefenseAssignmentService = supervisorDefenseAssignmentService;
        this.defenseScheduleConfigDAO = defenseScheduleConfigDAO;
        this.defenseScheduleConfigMapper = defenseScheduleConfigMapper;
    }

    @Override
    @Transactional
    public void createDefenseScheduleConfig(String studyYear, DefenseScheduleConfigDTO defenseScheduleConfig) {
        DefenseScheduleConfig defenseScheduleConfigEntity = defenseScheduleConfigMapper.mapToEntity(defenseScheduleConfig);
        defenseScheduleConfigEntity.setStudyYear(studyYear);
        defenseScheduleConfigEntity.setDefensePhase(DefensePhase.SCHEDULE_PLANNING);
        defenseScheduleConfigEntity = defenseScheduleConfigDAO.save(defenseScheduleConfigEntity);
        log.info("Defense schedule config was created with id: {}", defenseScheduleConfigEntity.getId());

        defenseTimeSlotService.createDefenseTimeSlots(studyYear, defenseScheduleConfigEntity.getId());
        supervisorDefenseAssignmentService.createSupervisorDefenseAssignments(studyYear, defenseScheduleConfigEntity.getId());
    }

    @Override
    @Transactional
    public void openRegistrationForDefense(String studyYear) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndDefensePhase(studyYear, DefensePhase.SCHEDULE_PLANNING);
        if (Objects.isNull(defenseScheduleConfig)) {
            log.error("Opening registration for project defense failed - defense schedule process is in incorrect phase for study year: {}", studyYear);
            throw new BusinessException("Opening registration for defense unsuccessful - process in incorrect phase");
        }
        projectDefenseService.createProjectDefenses(defenseScheduleConfig.getId(), studyYear);

        defenseScheduleConfig.setDefensePhase(DefensePhase.DEFENSE_PROJECT_REGISTRATION);
        defenseScheduleConfigDAO.save(defenseScheduleConfig);
    }

}