package pl.edu.amu.wmi.service.projectdefense.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ProjectDefenseDAO;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.service.defensetimeslot.DefenseTimeSlotService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectDefenseServiceImpl implements ProjectDefenseService {

    private final DefenseTimeSlotService defenseTimeSlotService;
    private final ProjectDefenseDAO projectDefenseDAO;

    public ProjectDefenseServiceImpl(DefenseTimeSlotService defenseTimeSlotService,
                                     ProjectDefenseDAO projectDefenseDAO) {
        this.defenseTimeSlotService = defenseTimeSlotService;
        this.projectDefenseDAO = projectDefenseDAO;
    }

    @Override
    @Transactional
    public void createProjectDefenses(Long defenseScheduleConfigId, String studyYear) {
        List<DefenseTimeSlot> timeSlots = defenseTimeSlotService.getAllTimeSlotsForDefenseConfig(defenseScheduleConfigId);

        timeSlots.forEach(defenseTimeSlot ->
            createProjectDefensesForTimeSlot(studyYear, defenseTimeSlot)
        );
        log.info("Project defense slots have been created for study year {}", studyYear);
    }

    private void createProjectDefensesForTimeSlot(String studyYear, DefenseTimeSlot defenseTimeSlot) {
        Map<CommitteeIdentifier, List<SupervisorDefenseAssignment>> committeeMap = mapCommitteesByCommitteeIdentifiers(defenseTimeSlot);
        committeeMap.forEach((committeeIdentifier, supervisorDefenseAssignments) -> {
            if (!supervisorDefenseAssignments.isEmpty()) {
                if (!isChairpersonSetCorrectlyForCommittee(supervisorDefenseAssignments, defenseTimeSlot, committeeIdentifier)) {
                    throw new BusinessException(MessageFormat.format("Project defense for committee with identifier: " +
                                    "{0} for time slot {1} {2} cannot be created. Committee has to have exactly one chairperson selected",
                            committeeIdentifier, defenseTimeSlot.getDate(), defenseTimeSlot.getStartTime()));
                }
                createNewProjectDefense(studyYear, supervisorDefenseAssignments);
            }
        });
    }

    private void createNewProjectDefense(String studyYear, List<SupervisorDefenseAssignment> supervisorDefenseAssignments) {
        ProjectDefense projectDefense = new ProjectDefense();
        projectDefense.addSupervisorDefenseAssignments(supervisorDefenseAssignments);
        projectDefense.setStudyYear(studyYear);
        projectDefenseDAO.save(projectDefense);
    }

    private Map<CommitteeIdentifier, List<SupervisorDefenseAssignment>> mapCommitteesByCommitteeIdentifiers(DefenseTimeSlot defenseTimeSlot) {
        return defenseTimeSlot.getSupervisorDefenseAssignments().stream()
                .filter(supervisorDefenseAssignment -> Objects.nonNull(supervisorDefenseAssignment.getCommitteeIdentifier()))
                .collect(Collectors.groupingBy(SupervisorDefenseAssignment::getCommitteeIdentifier));
    }

    private boolean isChairpersonSetCorrectlyForCommittee(List<SupervisorDefenseAssignment> supervisorDefenseAssignments,
                                                          DefenseTimeSlot defenseTimeSlot, CommitteeIdentifier committeeIdentifier) {
        long numberOfChairpersonsInCommittee = supervisorDefenseAssignments.stream()
                .filter(SupervisorDefenseAssignment::isChairperson)
                .count();
        if (numberOfChairpersonsInCommittee == 0) {
            log.error("Project defense for committee with identifier: {} for time slot {} {} cannot be created because none chairperson was selected.",
                    committeeIdentifier, defenseTimeSlot.getDate(), defenseTimeSlot.getStartTime());
            return false;
        } else if (numberOfChairpersonsInCommittee > 1) {
            log.error("Project defense for committee with identifier: {} for time slot {} {} cannot be created because more than one chairperson was selected.",
                    committeeIdentifier, defenseTimeSlot.getDate(), defenseTimeSlot.getStartTime());
            return false;
        } else {
            return true;
        }
    }
}
