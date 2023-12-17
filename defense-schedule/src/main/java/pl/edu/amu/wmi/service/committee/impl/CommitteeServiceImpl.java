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
import pl.edu.amu.wmi.mapper.committee.SupervisorAvailabilityMapper;
import pl.edu.amu.wmi.model.CommitteeAssignmentCriteria;
import pl.edu.amu.wmi.model.committee.ChairpersonAssignmentDTO;
import pl.edu.amu.wmi.model.committee.SupervisorDefenseAssignmentDTO;
import pl.edu.amu.wmi.service.committee.CommitteeService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;
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
                                DefenseScheduleConfigDAO defenseScheduleConfigDAO,
                                CommitteeMemberDAO committeeMemberDAO,
                                ProjectDefenseService projectDefenseService) {

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

        supervisorDefenseAssignmentDTOs.forEach(sda -> {
            SupervisorDefenseAssignment committeeMember = supervisorDefenseAssignmentDAO.findBySupervisor_IdAndDefenseTimeSlot_Id(Long.valueOf(sda.getSupervisorId()), sda.getDefenseSlotId());

            List<SupervisorDefenseAssignment> committeesWhereCommitteeMemberIsAChairperson = new ArrayList<>();
            if (!committeeMember.isChairperson() && Objects.nonNull(sda.getCommitteeIdentifier())) {
                committeesWhereCommitteeMemberIsAChairperson =
                        findCommitteeMemberAssignmentsWhenIsAChairpersonOfOtherCommitteeDuringTheDay(studyYear, committeeMember, sda.getCommitteeIdentifier());
            }

            CommitteeUpdateCase updateCase = determineCommitteeUpdateCase(sda, committeeMember, committeesWhereCommitteeMemberIsAChairperson);

            switch (updateCase) {
                case COMMITTEE_MEMBER_ASSIGNMENT_NOT_CHANGED -> {}
                case CHAIRPERSON_COMMITTEE_SLOT_DELETED -> deleteProjectDefensesConnectedWithChairperson(committeeMember.getCommitteeIdentifier(), studyYear, null, committeeMember.getDefenseTimeSlot().getId());
                case COMMITTEE_MEMBER_ASSIGNMENT_DELETED -> {
                    committeeMember.setCommitteeIdentifier(null);
                    supervisorDefenseAssignmentDAO.save(committeeMember);
                }
                case CHAIRPERSON_COMMITTEE_SLOT_DELETED_WITH_PROJECT_UNASSIGNMENT -> {
                    // TODO: 12/16/2023 add implementation
                }
                case COMMITTEE_MEMBER_ASSIGNMENT_CHANGE, COMMITTEE_MEMBER_ASSIGNMENT_CREATED -> {
                    committeeMember.setCommitteeIdentifier(sda.getCommitteeIdentifier());
                    supervisorDefenseAssignmentDAO.save(committeeMember);
                }
                case CHAIRPERSON_ASSIGNMENT_CHANGED_AND_PREVIOUS_COMMITTEE_SLOT_DELETED -> {
                    deleteProjectDefensesConnectedWithChairperson(committeeMember.getCommitteeIdentifier(), studyYear, null, committeeMember.getDefenseTimeSlot().getId());
                    committeeMember = supervisorDefenseAssignmentDAO.findBySupervisor_IdAndDefenseTimeSlot_Id(Long.valueOf(sda.getSupervisorId()), sda.getDefenseSlotId());
                    committeeMember.setCommitteeIdentifier(sda.getCommitteeIdentifier());
                    supervisorDefenseAssignmentDAO.save(committeeMember);
                    // TODO: 12/16/2023 check if committee for time slot exists
                }
                case CHAIRPERSON_COMMITTEE_SLOT_CREATED -> {
                    String classroom = committeesWhereCommitteeMemberIsAChairperson.get(0).getClassroom();
                    committeeMember.setCommitteeIdentifier(sda.getCommitteeIdentifier());
                    committeeMember.setChairperson(Boolean.TRUE);
                    committeeMember.setClassroom(classroom);
                    supervisorDefenseAssignmentDAO.save(committeeMember);
                    List<SupervisorDefenseAssignment> otherCommitteeMembers = findOtherCommitteeMembers(
                            committeeMember.getCommitteeIdentifier(),
                            committeeMember.getDefenseTimeSlot().getId(),
                            committeeMember.getId()
                    );
                    otherCommitteeMembers.forEach(otherMember -> {
                        otherMember.setClassroom(classroom);
                    });
                    otherCommitteeMembers = supervisorDefenseAssignmentDAO.saveAll(otherCommitteeMembers);
                    List<SupervisorDefenseAssignment> committeeMembers = new ArrayList<>();
                    committeeMembers.addAll(otherCommitteeMembers);
                    committeeMembers.add(committeeMember);
                    projectDefenseService.createProjectDefense(studyYear, committeeMembers);
                }
            }
        });

    }

    private CommitteeUpdateCase determineCommitteeUpdateCase(SupervisorDefenseAssignmentDTO sda, SupervisorDefenseAssignment committeeMember,
                                                             List<SupervisorDefenseAssignment> committeesWhereCommitteeMemberIsAChairperson) {

        if (Objects.equals(sda.getCommitteeIdentifier(), committeeMember.getCommitteeIdentifier())) {
            return CommitteeUpdateCase.COMMITTEE_MEMBER_ASSIGNMENT_NOT_CHANGED;
        } else if (Objects.isNull(sda.getCommitteeIdentifier()) && committeeMember.isChairperson() && Objects.isNull(committeeMember.getProjectDefense().getProject())) {
            return CommitteeUpdateCase.CHAIRPERSON_COMMITTEE_SLOT_DELETED;
        } else if (Objects.isNull(sda.getCommitteeIdentifier()) && committeeMember.isChairperson() && Objects.nonNull(committeeMember.getProjectDefense().getProject())) {
            return CommitteeUpdateCase.CHAIRPERSON_COMMITTEE_SLOT_DELETED_WITH_PROJECT_UNASSIGNMENT;
        } else if (Objects.isNull(sda.getCommitteeIdentifier()) && !committeeMember.isChairperson()) {
            return CommitteeUpdateCase.COMMITTEE_MEMBER_ASSIGNMENT_DELETED;
        } else if (Objects.nonNull(committeeMember.getCommitteeIdentifier()) && Objects.nonNull(committeeMember.getCommitteeIdentifier())
                && !Objects.equals(committeeMember.getCommitteeIdentifier(), sda.getCommitteeIdentifier())
                && committeesWhereCommitteeMemberIsAChairperson.isEmpty() && !committeeMember.isChairperson()) {
            return CommitteeUpdateCase.COMMITTEE_MEMBER_ASSIGNMENT_CHANGE;
        } else if (Objects.nonNull(committeeMember.getCommitteeIdentifier()) && Objects.nonNull(committeeMember.getCommitteeIdentifier())
                && !Objects.equals(committeeMember.getCommitteeIdentifier(), sda.getCommitteeIdentifier())
                && committeesWhereCommitteeMemberIsAChairperson.isEmpty() && committeeMember.isChairperson()) {
            return CommitteeUpdateCase.CHAIRPERSON_ASSIGNMENT_CHANGED_AND_PREVIOUS_COMMITTEE_SLOT_DELETED;
        } else if (!committeeMember.isChairperson() && !committeesWhereCommitteeMemberIsAChairperson.isEmpty()) {
            return CommitteeUpdateCase.CHAIRPERSON_COMMITTEE_SLOT_CREATED;
        } else if (!committeeMember.isChairperson() && Objects.isNull(committeeMember.getCommitteeIdentifier()) && committeesWhereCommitteeMemberIsAChairperson.isEmpty()) {
            return CommitteeUpdateCase.COMMITTEE_MEMBER_ASSIGNMENT_CREATED;
        }
        // TODO: 12/16/2023 remove null value (only for tests purposes)
        return null;
    }

    @Override
    public Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> getAggregatedChairpersonAssignments(String studyYear) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        if (Objects.isNull(defenseScheduleConfig)) {
            log.info("Defense schedule has been not configured yet for the study year {}", studyYear);
            return null;
        }
        List<Tuple> committeeChairpersonsPerDay = committeeMemberDAO.findCommitteeChairpersonsPerDayAndPerStudyYear(studyYear);
        List<ChairpersonAssignmentDTO> chairpersonAssignmentDTOs = mapTuplesToChairpersonDTOs(committeeChairpersonsPerDay);

        return createChairpersonPerCommitteeIdentifierPerDayMap(chairpersonAssignmentDTOs, studyYear, defenseScheduleConfig);
    }

    @Override
    @Transactional
    public void updateChairpersonAssignment(ChairpersonAssignmentDTO chairpersonAssignmentDTO, String studyYear) {
        LocalDate date = LocalDate.parse(chairpersonAssignmentDTO.getDate(), commonDateFormatter());

        if (Objects.isNull(chairpersonAssignmentDTO.getChairpersonId())) {
            deleteProjectDefensesConnectedWithChairperson(chairpersonAssignmentDTO.getCommitteeIdentifier(), studyYear, date, null);

        } else {
            CommitteeAssignmentCriteria criteria = CommitteeAssignmentCriteria.builder()
                    .supervisorId(Long.valueOf(chairpersonAssignmentDTO.getChairpersonId()))
                    .committeeIdentifier(chairpersonAssignmentDTO.getCommitteeIdentifier())
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

    private List<SupervisorDefenseAssignment> findCommitteeMemberAssignmentsWhenIsAChairpersonOfOtherCommitteeDuringTheDay(String studyYear, SupervisorDefenseAssignment entity, CommitteeIdentifier committeeIdentifier) {
        CommitteeAssignmentCriteria criteria = CommitteeAssignmentCriteria.builder()
                .committeeIdentifier(committeeIdentifier)
                .date(entity.getDefenseTimeSlot().getDate())
                .studyYear(studyYear)
                .isChairperson(Boolean.TRUE)
                .build();
        List<SupervisorDefenseAssignment> assignmentsByCriteria = committeeMemberDAO.findAllAssignmentsByCriteria(criteria);
        return assignmentsByCriteria;
    }

    private List<SupervisorDefenseAssignment> updateCommitteeMembers(ChairpersonAssignmentDTO chairpersonAssignmentDTO, SupervisorDefenseAssignment chairpersonAssignment, String studyYear, LocalDate date) {

        List<SupervisorDefenseAssignment> otherCommitteeMembers = findOtherCommitteeMembers(
                chairpersonAssignmentDTO.getCommitteeIdentifier(),
                chairpersonAssignment.getDefenseTimeSlot().getId(),
                Long.valueOf(chairpersonAssignmentDTO.getChairpersonId())
        );

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

    private List<SupervisorDefenseAssignment> findOtherCommitteeMembers(CommitteeIdentifier committeeIdentifier, Long defenseTimeslotId, Long chairpersonId) {
        CommitteeAssignmentCriteria otherCommitteeMembersCriteria = CommitteeAssignmentCriteria.builder()
                .committeeIdentifier(committeeIdentifier)
                .defenseTimeslotId(defenseTimeslotId)
                .excludedSupervisorIds(List.of(chairpersonId))
                .build();
        return committeeMemberDAO.findAllAssignmentsByCriteria(otherCommitteeMembersCriteria);
    }

    private void deleteProjectDefensesConnectedWithChairperson(CommitteeIdentifier committeeIdentifier, String studyYear, LocalDate date, Long defenseTimeslotId) {
        CommitteeAssignmentCriteria criteria = CommitteeAssignmentCriteria.builder()
                .date(date)
                .defenseTimeslotId(defenseTimeslotId)
                .committeeIdentifier(committeeIdentifier)
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

    private Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> createChairpersonPerCommitteeIdentifierPerDayMap(
            List<ChairpersonAssignmentDTO> chairpersonAssignmentDTOs, String studyYear, DefenseScheduleConfig defenseScheduleConfig) {
        Map<String, List<ChairpersonAssignmentDTO>> chairpersonPerDayMap = chairpersonAssignmentDTOs.stream()
                .collect(Collectors.groupingBy(ChairpersonAssignmentDTO::getDate));

        Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> chairpersonPerCommitteeIdentifierPerDayMap = createMapTemplate(studyYear, defenseScheduleConfig);

        chairpersonPerDayMap.forEach((date, chairpersons) -> {
            Map<CommitteeIdentifier, ChairpersonAssignmentDTO> chairpersonPerCommitteeIdentifier = chairpersons.stream()
                    .collect(Collectors.groupingBy(ChairpersonAssignmentDTO::getCommitteeIdentifier,
                            Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0))));
            chairpersonPerCommitteeIdentifier.forEach((committeeIdentifier, chairperson) -> {
                chairpersonPerCommitteeIdentifierPerDayMap.get(date).put(committeeIdentifier, chairperson);
            });
        });

        return chairpersonPerCommitteeIdentifierPerDayMap;
    }

    private Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> createMapTemplate(String studyYear, DefenseScheduleConfig defenseScheduleConfig) {
        Map<String, Map<CommitteeIdentifier, ChairpersonAssignmentDTO>> mapTemplate = new TreeMap<>();
        List<LocalDate> dates = getDefenseDays(defenseScheduleConfig.getStartDate(), defenseScheduleConfig.getEndDate());
        dates.forEach(day -> {
            Map<CommitteeIdentifier, ChairpersonAssignmentDTO> map = new TreeMap<>();
            Arrays.stream(CommitteeIdentifier.values()).forEach(committeeIdentifier -> {
                map.put(committeeIdentifier, new ChairpersonAssignmentDTO(committeeIdentifier, day.format(commonDateFormatter())));
            });
            mapTemplate.put(day.format(commonDateFormatter()), map);
        });
        return mapTemplate;
    }

    private List<LocalDate> getDefenseDays(LocalDate startDate, LocalDate endDate) {
        long numOfDaysBetween = Duration.ofDays(DAYS.between(startDate, endDate)).toDays();

        return IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween + 1)
                .mapToObj(startDate::plusDays)
                .toList();
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

    private enum CommitteeUpdateCase {
        COMMITTEE_MEMBER_ASSIGNMENT_NOT_CHANGED,
        COMMITTEE_MEMBER_ASSIGNMENT_DELETED,
        COMMITTEE_MEMBER_ASSIGNMENT_CHANGE,
        COMMITTEE_MEMBER_ASSIGNMENT_CREATED,
        CHAIRPERSON_COMMITTEE_SLOT_DELETED,
        CHAIRPERSON_ASSIGNMENT_CHANGED_AND_PREVIOUS_COMMITTEE_SLOT_DELETED,
        CHAIRPERSON_COMMITTEE_SLOT_DELETED_WITH_PROJECT_UNASSIGNMENT,
        CHAIRPERSON_COMMITTEE_SLOT_CREATED

    }

}
