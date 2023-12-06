package pl.edu.amu.wmi.service.supervisordefense.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.dao.SupervisorDefenseAssignmentDAO;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.service.defensetimeslot.DefenseTimeSlotService;
import pl.edu.amu.wmi.service.supervisordefense.SupervisorDefenseAssignmentService;

import java.text.MessageFormat;
import java.util.List;

@Service
@Slf4j
public class SupervisorDefenseAssignmentServiceImpl implements SupervisorDefenseAssignmentService {

    private final DefenseTimeSlotService defenseTimeSlotService;
    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;
    private final SupervisorDAO supervisorDAO;

    @Autowired
    public SupervisorDefenseAssignmentServiceImpl(DefenseTimeSlotService defenseTimeSlotService,
                                                  SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO,
                                                  SupervisorDAO supervisorDAO) {
        this.defenseTimeSlotService = defenseTimeSlotService;
        this.supervisorDefenseAssignmentDAO = supervisorDefenseAssignmentDAO;
        this.supervisorDAO = supervisorDAO;
    }

    /**
     * Create supervisor defense assignment for all supervisors and all defense timeslots.
     */
    @Override
    @Transactional
    public void createSupervisorDefenseAssignments(String studyYear, Long defenseScheduleConfigId) {
        List<Supervisor> supervisors = supervisorDAO.findAllByStudyYear(studyYear);

        if (supervisors.isEmpty())
            throw new BusinessException(MessageFormat.format("Supervisors for study year: {0} were not found", studyYear));

        List<DefenseTimeSlot> defenseTimeSlots = defenseTimeSlotService.getAllTimeSlotsForDefenseConfig(defenseScheduleConfigId);

        if (defenseTimeSlots.isEmpty())
            throw new BusinessException(MessageFormat.format("Time slots for defense schedule config with id: {0} were not found", defenseScheduleConfigId));

        supervisors.forEach(supervisor -> {
            defenseTimeSlots.forEach(timeslot -> {
                SupervisorDefenseAssignment supervisorDefenseAssignment = createSingleSupervisorDefenseAssignment(supervisor, timeslot);
                supervisorDefenseAssignmentDAO.save(supervisorDefenseAssignment);
            });
            log.info("Supervisor defense assignments were created for supervisor with id: {}", supervisor.getId());
        });
    }

    private SupervisorDefenseAssignment createSingleSupervisorDefenseAssignment(Supervisor supervisor, DefenseTimeSlot defenseTimeSlot) {
        SupervisorDefenseAssignment supervisorDefenseAssignment = new SupervisorDefenseAssignment();
        supervisorDefenseAssignment.setSupervisor(supervisor);
        supervisorDefenseAssignment.setDefenseTimeSlot(defenseTimeSlot);
        return supervisorDefenseAssignment;
    }
}
