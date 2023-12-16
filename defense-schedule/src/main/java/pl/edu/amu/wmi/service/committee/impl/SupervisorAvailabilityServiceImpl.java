package pl.edu.amu.wmi.service.committee.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.dao.SupervisorDefenseAssignmentDAO;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.mapper.committee.SupervisorAvailabilityMapper;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.committee.SupervisorAvailabilityService;
import pl.edu.amu.wmi.service.committee.SupervisorDefenseAssignmentService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static pl.edu.amu.wmi.util.CommonDateFormatter.commonDateFormatter;

@Service
@Slf4j
public class SupervisorAvailabilityServiceImpl implements SupervisorAvailabilityService {

    private final SupervisorDefenseAssignmentService supervisorDefenseAssignmentService;
    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;
    private final SupervisorDAO supervisorDAO;
    private final SupervisorAvailabilityMapper supervisorAvailabilityMapper;

    @Autowired
    public SupervisorAvailabilityServiceImpl(SupervisorDefenseAssignmentService supervisorDefenseAssignmentService,
                                             SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO,
                                             SupervisorDAO supervisorDAO,
                                             SupervisorAvailabilityMapper supervisorAvailabilityMapper) {
        this.supervisorDefenseAssignmentService = supervisorDefenseAssignmentService;
        this.supervisorDefenseAssignmentDAO = supervisorDefenseAssignmentDAO;
        this.supervisorDAO = supervisorDAO;
        this.supervisorAvailabilityMapper = supervisorAvailabilityMapper;
    }

    @Override
    @Transactional
    public void putSupervisorAvailability(String studyYear, Long supervisorId, Map<String, SupervisorDefenseAssignmentDTO> supervisorDefenseAssignments) {
        supervisorDefenseAssignments.values().forEach(supervisorDefenseAssignment -> {
            Long changedTimeSlotId = supervisorDefenseAssignment.getDefenseSlotId();
            boolean isTimeSlotSelected = supervisorDefenseAssignment.isAvailable();

            SupervisorDefenseAssignment supervisorDefenseAssignmentEntity = supervisorDefenseAssignmentDAO.findBySupervisor_IdAndDefenseTimeSlot_Id(supervisorId, changedTimeSlotId);
            supervisorDefenseAssignmentEntity.setAvailable(isTimeSlotSelected);
            supervisorDefenseAssignmentDAO.save(supervisorDefenseAssignmentEntity);
            log.info("Supervisor availability was updated for supervisor with id: {}", supervisorId);
        });
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

    /**
     * Returns the aggregated availability of Supervisors for all Supervisors in a given study year.
     */
    @Override
    public Map<String, Map<String, Map<String, SupervisorDefenseAssignmentDTO>>> getAggregatedSupervisorsAvailability(String studyYear) {
        List<Supervisor> supervisorsByStudyYear = supervisorDAO.findAllByStudyYear(studyYear);
        List<LocalDate> defenseDays = supervisorDefenseAssignmentService.getAllDefenseAssignmentDaysForStudyYear(studyYear);

        return createAggregatedSupervisorAvailability(supervisorsByStudyYear, defenseDays);
    }

    /**
     * Creates aggregated Supervisor availability for days for which defense is scheduled.
     */
    private Map<String, Map<String, Map<String, SupervisorDefenseAssignmentDTO>>> createAggregatedSupervisorAvailability(List<Supervisor> supervisorsByStudyYear, List<LocalDate> defenseDays) {
        Map<String, Map<String, Map<String, SupervisorDefenseAssignmentDTO>>> aggregatedSupervisorsAvailability = new TreeMap<>();

        defenseDays.forEach(day -> {
            Map<String, Map<String, SupervisorDefenseAssignmentDTO>> supervisorAvailabilityByDay = createSupervisorsAvailabilityByDay(supervisorsByStudyYear, day);
            aggregatedSupervisorsAvailability.put(day.format(commonDateFormatter()), supervisorAvailabilityByDay);
        });

        return aggregatedSupervisorsAvailability;
    }

    /**
     * Creates the Supervisor's availability aggregated by the Supervisor's last name and then by the time of each
     * time slot on the selected day.
     */
    private Map<String, Map<String, SupervisorDefenseAssignmentDTO>> createSupervisorsAvailabilityByDay(List<Supervisor> supervisors, LocalDate day) {
        Map<String, Map<String, SupervisorDefenseAssignmentDTO>> supervisorAvailabilityPerDay = new TreeMap<>();
        supervisors.forEach(supervisor -> {
            Map<String, Map<String, SupervisorDefenseAssignmentDTO>> availabilitySurvey = getSupervisorAvailabilitySurvey(supervisor.getId());
            supervisorAvailabilityPerDay.put(String.valueOf(supervisor.getId()), availabilitySurvey.get(day.format(commonDateFormatter())));
        });

        return supervisorAvailabilityPerDay;
    }

}
