package pl.edu.amu.wmi.service.committee.impl;

import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ChairpersonDAO;
import pl.edu.amu.wmi.dao.DefenseScheduleConfigDAO;
import pl.edu.amu.wmi.dao.SupervisorDefenseAssignmentDAO;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.mapper.committee.SupervisorAvailabilityMapper;
import pl.edu.amu.wmi.model.committee.ChairpersonAssignmentDTO;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.committee.CommitteeService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static pl.edu.amu.wmi.util.CommonDateFormatter.commonDateFormatter;

@Slf4j
@Service
public class CommitteeServiceImpl implements CommitteeService {

    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;
    private final SupervisorAvailabilityMapper supervisorAvailabilityMapper;
    private final DefenseScheduleConfigDAO defenseScheduleConfigDAO;

    private final ChairpersonDAO chairpersonDAO;

    public CommitteeServiceImpl(SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO,
                                SupervisorAvailabilityMapper supervisorAvailabilityMapper,
                                DefenseScheduleConfigDAO defenseScheduleConfigDAO, ChairpersonDAO chairpersonDAO) {

        this.supervisorDefenseAssignmentDAO = supervisorDefenseAssignmentDAO;
        this.supervisorAvailabilityMapper = supervisorAvailabilityMapper;
        this.defenseScheduleConfigDAO = defenseScheduleConfigDAO;
        this.chairpersonDAO = chairpersonDAO;
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

    @Override
    public Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> getAggregatedChairpersonAssignments(String studyYear) {
        List<Tuple> committeeChairpersonsPerDay = chairpersonDAO.findCommitteeChairpersonsPerDayAndPerStudyYear(studyYear);
        List<ChairpersonAssignmentDTO> chairpersonAssignmentDTOs = mapTuplesToChairpersonDTOs(committeeChairpersonsPerDay);

        return createChairpersonPerCommitteeIdentifierPerDayMap(chairpersonAssignmentDTOs);
    }

    private Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> createChairpersonPerCommitteeIdentifierPerDayMap(List<ChairpersonAssignmentDTO> chairpersonAssignmentDTOs) {
        Map<String, List<ChairpersonAssignmentDTO>> chairpersonPerDayMap = chairpersonAssignmentDTOs.stream()
                .collect(Collectors.groupingBy(ChairpersonAssignmentDTO::getDate));

        Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> chairpersonPerCommitteeIdentifierPerDayMap = new TreeMap<>();

        chairpersonPerDayMap.forEach((date, chairpersons) -> {
            Map<CommitteeIdentifier, ChairpersonAssignmentDTO> chairpersonPerCommitteeIdentifier = chairpersons.stream()
                    .collect(Collectors.groupingBy(ChairpersonAssignmentDTO::getCommitteeIdentifier,
                            Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0))));
            chairpersonPerCommitteeIdentifierPerDayMap.put(date, chairpersonPerCommitteeIdentifier);
        });

        return chairpersonPerCommitteeIdentifierPerDayMap;
    }

    private List<ChairpersonAssignmentDTO> mapTuplesToChairpersonDTOs(List<Tuple> committeeChairpersonsPerDay) {
        return committeeChairpersonsPerDay.stream()
                .map(this::mapTupleToChairpersonDTO)
                .toList();
    }

    private ChairpersonAssignmentDTO mapTupleToChairpersonDTO(Tuple chairpersonData) {
        Supervisor chairperson = (Supervisor) chairpersonData.get("supervisor");
        LocalDate date = (LocalDate) chairpersonData.get("date");
        String classroom = (String) chairpersonData.get("classroom");
        CommitteeIdentifier committeeIdentifier = (CommitteeIdentifier) chairpersonData.get("committeeIdentifier");

        return new ChairpersonAssignmentDTO(
                chairperson.getId().toString(),
                chairperson.getInitials(),
                classroom,
                committeeIdentifier,
                date.format(commonDateFormatter())
        );
    }

}
