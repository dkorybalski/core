package pl.edu.amu.wmi.service.supervisoravailability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.SupervisorDefenseAssignmentDAO;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.supervisoravailability.SupervisorAvailabilityService;

@Service
@Slf4j
public class SupervisorAvailabilityServiceImpl implements SupervisorAvailabilityService {

    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;

    @Autowired
    public SupervisorAvailabilityServiceImpl(SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO) {
        this.supervisorDefenseAssignmentDAO = supervisorDefenseAssignmentDAO;
    }

    @Override
    @Transactional
    public void putSupervisorAvailability(String studyYear, Long supervisorId, SupervisorDefenseAssignmentDTO supervisorDefenseAssignment) {
        Long changedTimeSlotId = supervisorDefenseAssignment.getDefenseSlotId();
        boolean isTimeSlotSelected = supervisorDefenseAssignment.isAvailable();

        SupervisorDefenseAssignment supervisorDefenseAssignmentEntity = supervisorDefenseAssignmentDAO.findBySupervisor_IdAndDefenseTimeSlot_Id(supervisorId, changedTimeSlotId);
        supervisorDefenseAssignmentEntity.setAvailable(isTimeSlotSelected);
        supervisorDefenseAssignmentDAO.save(supervisorDefenseAssignmentEntity);
        log.info("Supervisor availability was updated for supervisor with id: {}", supervisorId);
    }

}
