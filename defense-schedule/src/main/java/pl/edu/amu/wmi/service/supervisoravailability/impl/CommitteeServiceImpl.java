package pl.edu.amu.wmi.service.supervisoravailability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.DefenseScheduleConfigDAO;
import pl.edu.amu.wmi.dao.SupervisorDefenseAssignmentDAO;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.mapper.supervisoravailability.SupervisorAvailabilityMapper;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.supervisoravailability.CommitteeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommitteeServiceImpl implements CommitteeService {

    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;
    private final SupervisorAvailabilityMapper supervisorAvailabilityMapper;
    private final DefenseScheduleConfigDAO defenseScheduleConfigDAO;

    public CommitteeServiceImpl(SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO, SupervisorAvailabilityMapper supervisorAvailabilityMapper, DefenseScheduleConfigDAO defenseScheduleConfigDAO) {
        this.supervisorDefenseAssignmentDAO = supervisorDefenseAssignmentDAO;
        this.supervisorAvailabilityMapper = supervisorAvailabilityMapper;
        this.defenseScheduleConfigDAO = defenseScheduleConfigDAO;
    }

    @Override
    @Transactional
    public void updateCommittee(String studyYear, Map<String, SupervisorDefenseAssignmentDTO> supervisorDefenseAssignmentDTOMap) {
        List<SupervisorDefenseAssignmentDTO> supervisorDefenseAssignmentDTOs = new ArrayList<>(supervisorDefenseAssignmentDTOMap.values());
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        DefensePhase defensePhase = defenseScheduleConfig.getDefensePhase();

        switch (defensePhase) {
            case SCHEDULE_PLANNING -> {
                supervisorDefenseAssignmentDTOs.forEach(sda -> {
                    SupervisorDefenseAssignment entity = supervisorDefenseAssignmentDAO.findBySupervisor_IdAndDefenseTimeSlot_Id(sda.getSupervisorId(), sda.getDefenseSlotId());
                    supervisorAvailabilityMapper.update(entity, sda);
                    supervisorDefenseAssignmentDAO.save(entity);
                });
            }
            case DEFENSE_PROJECT_REGISTRATION, DEFENSE_PROJECT -> {
                supervisorDefenseAssignmentDTOs.forEach(sda -> {
                    SupervisorDefenseAssignment entity = supervisorDefenseAssignmentDAO.findBySupervisor_IdAndDefenseTimeSlot_Id(sda.getSupervisorId(), sda.getDefenseSlotId());
                    // TODO: 12/13/2023 add handling in case when dto.committeeIdentifier is set to null and there is a project assigned to the timeslot and supervisor is a chairperson
                    supervisorAvailabilityMapper.update(entity, sda);
                    supervisorDefenseAssignmentDAO.save(entity);
                });
            }
            case CLOSED -> log.info("Committee update in phase {} is not supported", defensePhase);
        }
    }

}
