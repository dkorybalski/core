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
import pl.edu.amu.wmi.model.scheduleconfig.DefensePhaseDTO;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleConfigDTO;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleModificationDTO;
import pl.edu.amu.wmi.service.committee.SupervisorDefenseAssignmentService;
import pl.edu.amu.wmi.service.defensetimeslot.DefenseTimeSlotService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;
import pl.edu.amu.wmi.service.scheduleconfig.DefenseScheduleConfigService;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;

import static pl.edu.amu.wmi.util.CommonDateUtils.commonDateFormatter;


@Slf4j
@Service
public class DefenseScheduleConfigServiceImpl implements DefenseScheduleConfigService {

    private final DefenseTimeSlotService defenseTimeSlotService;
    private final SupervisorDefenseAssignmentService supervisorDefenseAssignmentService;
    private final DefenseScheduleConfigDAO defenseScheduleConfigDAO;
    private final DefenseScheduleConfigMapper defenseScheduleConfigMapper;
    private final ProjectDefenseService projectDefenseService;


    @Autowired
    public DefenseScheduleConfigServiceImpl(DefenseTimeSlotService defenseTimeSlotService,
                                            SupervisorDefenseAssignmentService supervisorDefenseAssignmentService,
                                            DefenseScheduleConfigDAO defenseScheduleConfigDAO,
                                            DefenseScheduleConfigMapper defenseScheduleConfigMapper, ProjectDefenseService projectDefenseService) {
        this.defenseTimeSlotService = defenseTimeSlotService;
        this.supervisorDefenseAssignmentService = supervisorDefenseAssignmentService;
        this.defenseScheduleConfigDAO = defenseScheduleConfigDAO;
        this.defenseScheduleConfigMapper = defenseScheduleConfigMapper;
        this.projectDefenseService = projectDefenseService;
    }

    @Override
    @Transactional
    public void createDefenseScheduleConfig(String studyYear, DefenseScheduleConfigDTO defenseScheduleConfig) {
        DefenseScheduleConfig defenseScheduleConfigEntity = defenseScheduleConfigMapper.mapToEntity(defenseScheduleConfig);
        defenseScheduleConfigEntity.setStudyYear(studyYear);
        defenseScheduleConfigEntity.setDefensePhase(DefensePhase.SCHEDULE_PLANNING);
        defenseScheduleConfigEntity.setActive(true);
        defenseScheduleConfigEntity = defenseScheduleConfigDAO.save(defenseScheduleConfigEntity);
        log.info("Defense schedule config was created with id: {}", defenseScheduleConfigEntity.getId());

        defenseTimeSlotService.createDefenseTimeSlots(studyYear, defenseScheduleConfigEntity.getId());
        supervisorDefenseAssignmentService.createSupervisorDefenseAssignments(studyYear, defenseScheduleConfigEntity.getId(), null);
    }

    @Override
    @Transactional
    public DefensePhaseDTO openRegistrationForDefense(String studyYear) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        if (Objects.isNull(defenseScheduleConfig)) {
            log.error("Opening registration for project defense failed - active defense schedule config was not found for study year: {}", studyYear);
            throw new BusinessException("Opening registration for defense unsuccessful - active defense schedule config was not found");
        }

        if (Objects.equals(defenseScheduleConfig.getDefensePhase(), DefensePhase.DEFENSE_PROJECT_REGISTRATION)) {
            log.error("Opening registration for project defense failed - registration already opened");
            throw new BusinessException("Opening registration for project defense failed - registration already opened");
        }

        defenseScheduleConfig.setDefensePhase(DefensePhase.DEFENSE_PROJECT_REGISTRATION);
        defenseScheduleConfig = defenseScheduleConfigDAO.save(defenseScheduleConfig);
        log.info("Registration for projects defenses was opened for study year: {}", studyYear);
        return new DefensePhaseDTO(defenseScheduleConfig.getDefensePhase().getPhaseName());
    }

    @Override
    @Transactional
    public DefensePhaseDTO closeRegistrationForDefense(String studyYear) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndDefensePhase(studyYear, DefensePhase.DEFENSE_PROJECT_REGISTRATION);
        if (Objects.isNull(defenseScheduleConfig)) {
            log.error("Closing registration for project defense failed - defense schedule process is in incorrect phase for study year: {}", studyYear);
            throw new BusinessException("Closing registration for defense unsuccessful - process in incorrect phase");
        }

        defenseScheduleConfig.setDefensePhase(DefensePhase.DEFENSE_PROJECT);
        defenseScheduleConfig = defenseScheduleConfigDAO.save(defenseScheduleConfig);
        log.info("Registration for projects defenses was closed for study year: {}", studyYear);
        return new DefensePhaseDTO(defenseScheduleConfig.getDefensePhase().getPhaseName());
    }

    @Override
    public DefensePhaseDTO getCurrentDefensePhase(String studyYear) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        if (Objects.isNull(defenseScheduleConfig)) {
            throw new BusinessException(MessageFormat.format("Active DefenseScheduleConfig for study year: {0} not found", studyYear));
        }
        return new DefensePhaseDTO(defenseScheduleConfig.getDefensePhase().getPhaseName());
    }

    @Override
    @Transactional
    public void deleteActiveScheduleConfig(String studyYear) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        if (Objects.isNull(defenseScheduleConfig)) {
            throw new BusinessException(MessageFormat.format("Active DefenseScheduleConfig for study year: {0} not found", studyYear));
        }
        Long defenseScheduleConfigId = defenseScheduleConfig.getId();
        try {
            // delete supervisor availability and related project defenses
            supervisorDefenseAssignmentService.deleteAllConnectedWithDefenseScheduleConfig(defenseScheduleConfigId);
            // delete defense time slot
            defenseTimeSlotService.deleteAllConnectedWithDefenseScheduleConfig(defenseScheduleConfigId);
            // delete defense schedule config
            defenseScheduleConfigDAO.delete(defenseScheduleConfig);
            log.info("All objects connected with defense schedule config: {} have been sucessfully deleted", defenseScheduleConfigId);
        } catch (Exception e) {
            log.error("Deleting defense schedule config: {} unsuccessful", defenseScheduleConfig, e);
            throw new BusinessException("Defense schedule config deletion unsuccessful");
        }


    }

    @Override
    @Transactional
    public void archiveDefenseScheduleConfig(String studyYear) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        if (Objects.isNull(defenseScheduleConfig)) {
            throw new BusinessException(MessageFormat.format("Active DefenseScheduleConfig for study year: {0} not found", studyYear));
        }
        projectDefenseService.archiveProjectDefenses(studyYear);
        defenseScheduleConfig.setActive(Boolean.FALSE);
        defenseScheduleConfigDAO.save(defenseScheduleConfig);
        log.info("Defense schedule config for study year: {} with id: {} has beed archived", studyYear, defenseScheduleConfig.getId());
    }

    @Override
    @Transactional
    public void modifyDefenseSchedule(String studyYear, DefenseScheduleModificationDTO defenseScheduleModificationDTO) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        if (Objects.isNull(defenseScheduleConfig)) {
            throw new BusinessException(MessageFormat.format("Active DefenseScheduleConfig for study year: {0} not found", studyYear));
        }
        if (Objects.isNull(defenseScheduleConfig.getAdditionalDays())) {
            defenseScheduleConfig.setAdditionalDays(Set.of(defenseScheduleModificationDTO.date().format(commonDateFormatter())));
        } else {
            defenseScheduleConfig.getAdditionalDays().add(defenseScheduleModificationDTO.date().format(commonDateFormatter()));
        }
        defenseScheduleConfigDAO.save(defenseScheduleConfig);
        defenseTimeSlotService.createDefenseTimeSlotsForASingleDefenseDay(
                studyYear, defenseScheduleConfig, defenseScheduleModificationDTO.date());
        supervisorDefenseAssignmentService.createSupervisorDefenseAssignments(
                studyYear, defenseScheduleConfig.getId(), defenseScheduleModificationDTO.date());
        log.info("Defense day {} was successfully added to an active defense schedule for a study year: {}", defenseScheduleModificationDTO.date(), studyYear);

    }

}
