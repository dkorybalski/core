package pl.edu.amu.wmi.service.supervisoravailability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.SupervisorDefenseAssignmentDAO;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.mapper.supervisoravailability.SupervisorAvailabilityMapper;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.supervisoravailability.SupervisorAvailabilityService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static pl.edu.amu.wmi.util.CommonDateFormatter.commonDateFormatter;

@Service
@Slf4j
public class SupervisorAvailabilityServiceImpl implements SupervisorAvailabilityService {

    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;
    private final SupervisorAvailabilityMapper supervisorAvailabilityMapper;

    @Autowired
    public SupervisorAvailabilityServiceImpl(SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO,
                                             SupervisorAvailabilityMapper supervisorAvailabilityMapper) {
        this.supervisorDefenseAssignmentDAO = supervisorDefenseAssignmentDAO;
        this.supervisorAvailabilityMapper = supervisorAvailabilityMapper;
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

    /**
     * Returns Supervisor availability survey for the supervisor of a given study year.
     */
    @Override
    public Map<String, Map<String, SupervisorDefenseAssignmentDTO>> getSupervisorAvailabilitySurvey(Long supervisorId) {
        List<SupervisorDefenseAssignment> supervisorDefenseAssignments = supervisorDefenseAssignmentDAO.findAllBySupervisor_Id(supervisorId);

        Map<LocalDate, List<SupervisorDefenseAssignment>> supervisorDefenseAssignmentsByDate = mapSupervisorDefenseAssignmentsByDate(supervisorDefenseAssignments);

        return createSupervisorAvailabilitySurvey(supervisorDefenseAssignmentsByDate);
    }

    /**
     * Creates Supervisor defense assignments map aggregated by assignment date.
     */
    private Map<LocalDate, List<SupervisorDefenseAssignment>> mapSupervisorDefenseAssignmentsByDate(List<SupervisorDefenseAssignment> supervisorDefenseAssignments) {
        return supervisorDefenseAssignments.stream()
                .collect(Collectors.groupingBy(sda -> sda.getDefenseTimeSlot().getDate()));
    }

    /**
     * Creates Supervisor availability survey based on Supervisor defense assignments aggregated by assignment date.
     */
    private Map<String, Map<String, SupervisorDefenseAssignmentDTO>> createSupervisorAvailabilitySurvey(Map<LocalDate, List<SupervisorDefenseAssignment>> defenseAssignmentsByDate) {
        Map<String, Map<String, SupervisorDefenseAssignmentDTO>> supervisorAvailabilitySurvey = new TreeMap<>();

        defenseAssignmentsByDate.forEach((key, value) -> supervisorAvailabilitySurvey.put(key.format(commonDateFormatter()), createDefenseAssignmentsByTime(value)));

        return supervisorAvailabilitySurvey;
    }

    /**
     * Creates Supervisor defense assignments per hour using the assignment list for the selected day.
     */
    private Map<String, SupervisorDefenseAssignmentDTO> createDefenseAssignmentsByTime(List<SupervisorDefenseAssignment> defenseAssignmentsByDate) {
        Map<String, SupervisorDefenseAssignmentDTO> defenseAssignmentsByTime = new TreeMap<>();
        List<SupervisorDefenseAssignmentDTO> defenseAssignments = supervisorAvailabilityMapper.mapToDtoList(defenseAssignmentsByDate);

        defenseAssignments.forEach(assignment -> defenseAssignmentsByTime.put(assignment.getTime(), assignment));

        return defenseAssignmentsByTime;
    }

}