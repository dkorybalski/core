package pl.edu.amu.wmi.service.supervisoravailability.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.ProjectDefenseDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.model.supervisordefense.SupervisorStatisticsDTO;
import pl.edu.amu.wmi.service.supervisoravailability.SupervisorStatisticsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SupervisorStatisticsServiceImpl implements SupervisorStatisticsService {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final SupervisorDAO supervisorDAO;
    private final ProjectDefenseDAO projectDefenseDAO;

    public SupervisorStatisticsServiceImpl(SupervisorDAO supervisorDAO, ProjectDefenseDAO projectDefenseDAO) {
        this.supervisorDAO = supervisorDAO;
        this.projectDefenseDAO = projectDefenseDAO;
    }

    @Override
    public List<SupervisorStatisticsDTO> getSupervisorStatistics(String studyYear) {
        List<Supervisor> supervisors = supervisorDAO.findAllByStudyYear(studyYear);
        List<ProjectDefense> projectDefenses = projectDefenseDAO.findAllByStudyYear(studyYear);
        Map<LocalDate, List<ProjectDefense>> projectDefenseByDateMap = projectDefenses.stream().collect(Collectors.groupingBy(projectDefense -> projectDefense.getDefenseTimeslot().getDate()));
        List<SupervisorStatisticsDTO> supervisorStatisticsDTOs = new ArrayList<>();

        supervisors.forEach(supervisor -> {
            SupervisorStatisticsDTO supervisorStatisticsDTO = createStatisticsForSupervisor(supervisor, projectDefenses, projectDefenseByDateMap);
            supervisorStatisticsDTOs.add(supervisorStatisticsDTO);
        });

        return supervisorStatisticsDTOs;
    }

    private SupervisorStatisticsDTO createStatisticsForSupervisor(Supervisor supervisor, List<ProjectDefense> projectDefenses, Map<LocalDate, List<ProjectDefense>> projectDefenseByDateMap) {
        int numberOfGroups = countTheNumberOfAcceptedSupervisorProjects(supervisor);
        int numberOfAssignedProjectDefenses = countTheNumberOfDefensesAssignedToSupervisor(supervisor, projectDefenses);
        double load = numberOfGroups == 0 ? 0.0 : (double) numberOfAssignedProjectDefenses / (double) numberOfGroups;
        Map<String, Integer> committeesPerDayMap = createSupervisorDefensesByDateMap(supervisor, projectDefenseByDateMap);

        return new SupervisorStatisticsDTO(
                supervisor.getFullName(),
                numberOfGroups,
                numberOfAssignedProjectDefenses,
                load,
                committeesPerDayMap
        );
    }

    private int countTheNumberOfAcceptedSupervisorProjects(Supervisor supervisor) {
        return supervisor.getProjects().stream()
                .filter(project -> Objects.equals(AcceptanceStatus.ACCEPTED, project.getAcceptanceStatus()))
                .toList().size();
    }

    private int countTheNumberOfDefensesAssignedToSupervisor(Supervisor supervisor, List<ProjectDefense> projectDefenses) {
        return (int) projectDefenses.stream()
                .filter(defense -> isProjectSupervisorCommitteeMember(defense, supervisor))
                .count();
    }

    private Map<String, Integer> createSupervisorDefensesByDateMap(Supervisor supervisor, Map<LocalDate, List<ProjectDefense>> projectDefenseByDateMap) {
        Map<String, Integer> committeessPerDayMap = new TreeMap<>();
        projectDefenseByDateMap.forEach((date, defenses) -> {
            int numberOfCommittees = countTheNumberOfDefensesAssignedToSupervisor(supervisor, defenses);
            committeessPerDayMap.put(date.format(dateTimeFormatter), numberOfCommittees);
        });
        return committeessPerDayMap;
    }

    // TODO: 12/10/2023 refactor needed - method should be taken from projectMember service
    private boolean isProjectSupervisorCommitteeMember(ProjectDefense projectDefense, Supervisor supervisor) {
        List<Supervisor> committeeMembers = projectDefense.getSupervisorDefenseAssignments().stream()
                .map(SupervisorDefenseAssignment::getSupervisor)
                .toList();
        return committeeMembers.contains(supervisor);
    }
}
