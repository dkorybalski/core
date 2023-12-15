package pl.edu.amu.wmi.service.committee.impl;

import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.CommitteeMemberDAO;
import pl.edu.amu.wmi.dao.DefenseScheduleConfigDAO;
import pl.edu.amu.wmi.dao.SupervisorDefenseAssignmentDAO;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.mapper.committee.SupervisorAvailabilityMapper;
import pl.edu.amu.wmi.model.CommitteeAssignmentCriteria;
import pl.edu.amu.wmi.model.committee.ChairpersonAssignmentDTO;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.committee.CommitteeService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static pl.edu.amu.wmi.util.CommonDateFormatter.commonDateFormatter;

@Slf4j
@Service
public class CommitteeServiceImpl implements CommitteeService {

    private final SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO;
    private final SupervisorAvailabilityMapper supervisorAvailabilityMapper;
    private final DefenseScheduleConfigDAO defenseScheduleConfigDAO;
    private final CommitteeMemberDAO committeeMemberDAO;
    private final ProjectDefenseService projectDefenseService;


    public CommitteeServiceImpl(SupervisorDefenseAssignmentDAO supervisorDefenseAssignmentDAO,
                                SupervisorAvailabilityMapper supervisorAvailabilityMapper,
                                DefenseScheduleConfigDAO defenseScheduleConfigDAO, CommitteeMemberDAO committeeMemberDAO, ProjectDefenseService projectDefenseService) {

        this.supervisorDefenseAssignmentDAO = supervisorDefenseAssignmentDAO;
        this.supervisorAvailabilityMapper = supervisorAvailabilityMapper;
        this.defenseScheduleConfigDAO = defenseScheduleConfigDAO;
        this.committeeMemberDAO = committeeMemberDAO;
        this.projectDefenseService = projectDefenseService;
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
        List<Tuple> committeeChairpersonsPerDay = committeeMemberDAO.findCommitteeChairpersonsPerDayAndPerStudyYear(studyYear);
        List<ChairpersonAssignmentDTO> chairpersonAssignmentDTOs = mapTuplesToChairpersonDTOs(committeeChairpersonsPerDay);

        return createChairpersonPerCommitteeIdentifierPerDayMap(chairpersonAssignmentDTOs);
    }

    @Override
    @Transactional
    public void updateChairpersonAssignment(ChairpersonAssignmentDTO chairpersonAssignmentDTO, String studyYear) {
        LocalDate date = LocalDate.parse(chairpersonAssignmentDTO.getDate(), commonDateFormatter());

        if (Objects.isNull(chairpersonAssignmentDTO.getChairpersonId())) {
            deleteProjectDefensesConnectedWithChairperson(chairpersonAssignmentDTO, studyYear, date);

        } else {
            CommitteeAssignmentCriteria criteria = CommitteeAssignmentCriteria.builder()
                    .supervisorId(Long.valueOf(chairpersonAssignmentDTO.getChairpersonId()))
                    .committeeIdentifier(chairpersonAssignmentDTO.getCommitteeIdentifier())
                    .date(date)
                    .studyYear(studyYear)
                    .build();

            List<SupervisorDefenseAssignment> chairpersonAssignments = committeeMemberDAO.findAllAssignmentsByCriteria(criteria);
            chairpersonAssignments.forEach(chairpersonAssignment -> {
                List<SupervisorDefenseAssignment> committeeMembersWithoutProjectDefense = updateCommitteeMembers(chairpersonAssignmentDTO, chairpersonAssignment, studyYear, date);
                if (!committeeMembersWithoutProjectDefense.isEmpty()) {
                    projectDefenseService.createProjectDefense(studyYear, committeeMembersWithoutProjectDefense);
                }
            });
        }
    }

    private List<SupervisorDefenseAssignment> updateCommitteeMembers(ChairpersonAssignmentDTO chairpersonAssignmentDTO, SupervisorDefenseAssignment chairpersonAssignment, String studyYear, LocalDate date) {

        CommitteeAssignmentCriteria otherCommitteeMembersCriteria = CommitteeAssignmentCriteria.builder()
                .committeeIdentifier(chairpersonAssignmentDTO.getCommitteeIdentifier())
                .defenseTimeslotId(chairpersonAssignment.getDefenseTimeSlot().getId())
                .excludedSupervisorIds(List.of(Long.valueOf(chairpersonAssignmentDTO.getChairpersonId())))
                .build();
        List<SupervisorDefenseAssignment> otherCommitteeMembers = committeeMemberDAO.findAllAssignmentsByCriteria(otherCommitteeMembersCriteria);

        otherCommitteeMembers.forEach(committeeMember -> {
            committeeMember.setChairperson(false);
            committeeMember.setClassroom(chairpersonAssignmentDTO.getClassroom());
        });
        otherCommitteeMembers = supervisorDefenseAssignmentDAO.saveAll(otherCommitteeMembers);
        chairpersonAssignment.setClassroom(chairpersonAssignmentDTO.getClassroom());
        chairpersonAssignment.setChairperson(Boolean.TRUE);
        chairpersonAssignment = supervisorDefenseAssignmentDAO.save(chairpersonAssignment);

        List<SupervisorDefenseAssignment> committeeMembers = new ArrayList<>();
        if (Objects.isNull(chairpersonAssignment.getProjectDefense())) {
            committeeMembers.addAll(otherCommitteeMembers);
            committeeMembers.add(chairpersonAssignment);
        }

        return committeeMembers;
    }

    private void deleteProjectDefensesConnectedWithChairperson(ChairpersonAssignmentDTO chairpersonAssignmentDTO, String studyYear, LocalDate date) {
        CommitteeAssignmentCriteria criteria = CommitteeAssignmentCriteria.builder()
                .date(date)
                .committeeIdentifier(chairpersonAssignmentDTO.getCommitteeIdentifier())
                .studyYear(studyYear)
                .build();

        List<SupervisorDefenseAssignment> supervisorDefenseAssignments = committeeMemberDAO.findAllAssignmentsByCriteria(criteria);
        List<Long> projectDefenseIdsToBeRemoved = extractProjectDefenseIdsForDeletion(supervisorDefenseAssignments);
        supervisorDefenseAssignments.forEach(CommitteeServiceImpl::resetSupervisorDefenseAssignmentData);
        supervisorDefenseAssignmentDAO.saveAll(supervisorDefenseAssignments);

        projectDefenseService.deleteProjectDefenses(projectDefenseIdsToBeRemoved);
    }

    private static void resetSupervisorDefenseAssignmentData(SupervisorDefenseAssignment supervisorDefenseAssignment) {
        supervisorDefenseAssignment.setChairperson(Boolean.FALSE);
        supervisorDefenseAssignment.setCommitteeIdentifier(null);
        supervisorDefenseAssignment.setProjectDefense(null);
        supervisorDefenseAssignment.setClassroom(null);
    }

    private static List<Long> extractProjectDefenseIdsForDeletion(List<SupervisorDefenseAssignment> supervisorDefenseAssignments) {
        return supervisorDefenseAssignments.stream()
                .filter(sda -> Objects.equals(Boolean.TRUE, sda.isChairperson()))
                .map(sda -> sda.getProjectDefense().getId())
                .toList();
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
