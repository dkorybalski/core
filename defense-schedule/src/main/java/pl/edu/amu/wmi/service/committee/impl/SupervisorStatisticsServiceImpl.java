package pl.edu.amu.wmi.service.committee.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.amu.wmi.dao.DefenseScheduleConfigDAO;
import pl.edu.amu.wmi.dao.ProjectDefenseDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.model.committee.SupervisorStatisticsDTO;
import pl.edu.amu.wmi.service.committee.SupervisorStatisticsService;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static pl.edu.amu.wmi.util.CommonDateUtils.getDateStringWithTheDayOfWeek;
import static pl.edu.amu.wmi.util.CommonDateUtils.getDefenseDays;

@Service
@Slf4j
public class SupervisorStatisticsServiceImpl implements SupervisorStatisticsService {

    private final SupervisorDAO supervisorDAO;
    private final ProjectDefenseDAO projectDefenseDAO;
    private final DefenseScheduleConfigDAO defenseScheduleConfigDAO;

    public SupervisorStatisticsServiceImpl(SupervisorDAO supervisorDAO, ProjectDefenseDAO projectDefenseDAO, DefenseScheduleConfigDAO defenseScheduleConfigDAO) {
        this.supervisorDAO = supervisorDAO;
        this.projectDefenseDAO = projectDefenseDAO;
        this.defenseScheduleConfigDAO = defenseScheduleConfigDAO;
    }

    @Override
    public List<SupervisorStatisticsDTO> getSupervisorStatistics(String studyYear) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        if (Objects.isNull(defenseScheduleConfig)) {
            log.info("Defense schedule has been not configured yet for the study year {}", studyYear);
            return null;
        }
        List<Supervisor> supervisors = supervisorDAO.findAllByStudyYear(studyYear);
        List<ProjectDefense> projectDefenses = projectDefenseDAO.findAllByStudyYearAndSupervisorDefenseAssignmentsNotEmpty(studyYear);
        Map<LocalDate, List<ProjectDefense>> projectDefenseByDateMap = projectDefenses.stream().collect(Collectors.groupingBy(projectDefense -> projectDefense.getDefenseTimeslot().getDate()));
        List<SupervisorStatisticsDTO> supervisorStatisticsDTOs = new ArrayList<>();
        Map<String, Integer> statisticsTemplateMap = createStatisticsTemplateMap(defenseScheduleConfig);

        supervisors.forEach(supervisor -> {
            SupervisorStatisticsDTO supervisorStatisticsDTO = createStatisticsForSupervisor(supervisor, projectDefenses, projectDefenseByDateMap, statisticsTemplateMap);
            supervisorStatisticsDTOs.add(supervisorStatisticsDTO);
        });

        return supervisorStatisticsDTOs;
    }

    private Map<String, Integer> createStatisticsTemplateMap(DefenseScheduleConfig defenseScheduleConfig) {
        Map<String, Integer> statisticsTemplateMap = new TreeMap<>();
        List<LocalDate> defenseDays = getDefenseDays(defenseScheduleConfig.getStartDate(), defenseScheduleConfig.getEndDate());
        defenseDays.forEach(defenseDay -> statisticsTemplateMap.put(getDateStringWithTheDayOfWeek(defenseDay), 0));
        return statisticsTemplateMap;
    }

    private SupervisorStatisticsDTO createStatisticsForSupervisor(Supervisor supervisor, List<ProjectDefense> projectDefenses, Map<LocalDate, List<ProjectDefense>> projectDefenseByDateMap, Map<String, Integer> statisticsTemplateMap) {
        int numberOfGroups = countTheNumberOfAcceptedSupervisorProjects(supervisor);
        int numberOfAssignedProjectDefenses = countTheNumberOfDefensesAssignedToSupervisor(supervisor, projectDefenses);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        double load = numberOfGroups == 0 ? 0.0 : Double.parseDouble(decimalFormat.format((double) numberOfAssignedProjectDefenses / (double) numberOfGroups));
        Map<String, Integer> committeesPerDayMap = createSupervisorDefensesByDateMap(supervisor, projectDefenseByDateMap, statisticsTemplateMap);

        return new SupervisorStatisticsDTO(
                supervisor.getInitials(),
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

    private Map<String, Integer> createSupervisorDefensesByDateMap(Supervisor supervisor, Map<LocalDate, List<ProjectDefense>> projectDefenseByDateMap, Map<String, Integer> statisticsTemplateMap) {
        Map<String, Integer> committeessPerDayMap = new TreeMap<>(statisticsTemplateMap);
        projectDefenseByDateMap.forEach((date, defenses) -> {
            int numberOfCommittees = countTheNumberOfDefensesAssignedToSupervisor(supervisor, defenses);
            committeessPerDayMap.put(getDateStringWithTheDayOfWeek(date), numberOfCommittees);
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
