package pl.edu.amu.wmi.service.committee.impl;

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
import pl.edu.amu.wmi.service.committee.SupervisorDefenseAssignmentService;
import pl.edu.amu.wmi.service.defensetimeslot.DefenseTimeSlotService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SupervisorDefenseAssignmentServiceImpl implements SupervisorDefenseAssignmentService {

    private final DefenseTimeSlotService defenseTimeSlotService;
    private final ProjectDefenseService projectDefenseService;
    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;
    private final SupervisorDAO supervisorDAO;

    @Autowired
    public SupervisorDefenseAssignmentServiceImpl(DefenseTimeSlotService defenseTimeSlotService,
                                                  ProjectDefenseService projectDefenseService, SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO,
                                                  SupervisorDAO supervisorDAO) {
        this.defenseTimeSlotService = defenseTimeSlotService;
        this.projectDefenseService = projectDefenseService;
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

    @Override
    public List<LocalDate> getAllDefenseAssignmentDaysForStudyYear(String studyYear) {
        List<SupervisorDefenseAssignment> allSupervisorDefenseAssignmentsForStudyYear =
                supervisorDefenseAssignmentDAO.findAllByDefenseTimeSlot_StudyYearAndDefenseTimeSlot_DefenseScheduleConfig_IsActiveIsTrue(studyYear);
        return allSupervisorDefenseAssignmentsForStudyYear.stream().map(defenseAssignment -> defenseAssignment.getDefenseTimeSlot().getDate()).distinct().toList();
    }

    @Override
    @Transactional
    public void deleteAllConnectedWithDefenseScheduleConfig(Long defenseScheduleConfigId) {
        List<SupervisorDefenseAssignment> supervisorDefenseAssignmentsToBeDeleted =
                supervisorDefenseAssignmentDAO.findAllByDefenseTimeSlot_DefenseScheduleConfig_Id(defenseScheduleConfigId);
        List<Long> projectDefenseIdsToBeDeleted = extractProjectDefenseIdsForDeletion(supervisorDefenseAssignmentsToBeDeleted);

        supervisorDefenseAssignmentDAO.deleteAll(supervisorDefenseAssignmentsToBeDeleted);
        projectDefenseService.deleteProjectDefenses(projectDefenseIdsToBeDeleted);

        log.info("Project defenses and supervisor defense assignments was deleted for defenseScheduleConfig: {}", defenseScheduleConfigId);
    }

    private List<Long> extractProjectDefenseIdsForDeletion(List<SupervisorDefenseAssignment> supervisorDefenseAssignments) {
        return supervisorDefenseAssignments.stream()
                .filter(sda -> Objects.equals(Boolean.TRUE, sda.isChairperson()))
                .map(sda -> sda.getProjectDefense().getId())
                .toList();
    }
}
