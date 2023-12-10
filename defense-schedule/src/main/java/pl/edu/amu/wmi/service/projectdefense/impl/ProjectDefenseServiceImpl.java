package pl.edu.amu.wmi.service.projectdefense.impl;

import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.DefenseScheduleConfigDAO;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.dao.ProjectDefenseDAO;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.enumerations.DefensePhase;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.mapper.projectdefense.ProjectDefenseMapper;
import pl.edu.amu.wmi.model.UserRoleType;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefensePatchDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseSummaryDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectNameDTO;
import pl.edu.amu.wmi.service.PermissionService;
import pl.edu.amu.wmi.service.ProjectMemberService;
import pl.edu.amu.wmi.service.defensetimeslot.DefenseTimeSlotService;
import pl.edu.amu.wmi.service.notification.DefenseNotificationService;
import pl.edu.amu.wmi.service.projectdefense.ProjectDefenseService;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectDefenseServiceImpl implements ProjectDefenseService {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final DefenseTimeSlotService defenseTimeSlotService;
    private final ProjectMemberService projectMemberService;
    private final PermissionService permissionService;
    private final DefenseNotificationService defenseNotificationService;
    private final DefenseScheduleConfigDAO defenseScheduleConfigDAO;
    private final ProjectDefenseMapper projectDefenseMapper;
    private final ProjectDefenseDAO projectDefenseDAO;
    private final ProjectDAO projectDAO;

    public ProjectDefenseServiceImpl(DefenseTimeSlotService defenseTimeSlotService,
                                     ProjectMemberService projectMemberService,
                                     PermissionService permissionService,
                                     DefenseNotificationService defenseNotificationService,
                                     DefenseScheduleConfigDAO defenseScheduleConfigDAO,
                                     ProjectDefenseMapper projectDefenseMapper,
                                     ProjectDefenseDAO projectDefenseDAO,
                                     ProjectDAO projectDAO) {
        this.defenseTimeSlotService = defenseTimeSlotService;
        this.projectMemberService = projectMemberService;
        this.permissionService = permissionService;
        this.defenseNotificationService = defenseNotificationService;
        this.defenseScheduleConfigDAO = defenseScheduleConfigDAO;
        this.projectDefenseMapper = projectDefenseMapper;
        this.projectDefenseDAO = projectDefenseDAO;
        this.projectDAO = projectDAO;
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

    @Override
    public Map<String, List<ProjectDefenseDTO>> getProjectDefenses(String studyYear, String indexNumber) {
        List<ProjectDefense> projectDefenses = projectDefenseDAO.findAllByStudyYear(studyYear);
        return createProjectDefenseDTOMap(studyYear, indexNumber, projectDefenses);
    }

    @Override
    public Map<String, List<ProjectDefenseSummaryDTO>> getProjectDefensesSummary(String studyYear) {
        List<ProjectDefense> projectDefenses = projectDefenseDAO.findAllByStudyYear(studyYear);
        Map<LocalDate, List<ProjectDefense>> projectDefenseMap = projectDefenses.stream().collect(Collectors.groupingBy(projectDefense -> projectDefense.getDefenseTimeslot().getDate()));
        Map<String, List<ProjectDefenseSummaryDTO>> projectDefenseDTOMap = new TreeMap<>();
        projectDefenseMap.forEach((date, defenses) -> {
            List<ProjectDefense> projectDefensesWithProjects = defenses.stream()
                    .filter(defense -> Objects.nonNull(defense.getProject()))
                    .toList();
            projectDefenseDTOMap.put(date.format(dateTimeFormatter), projectDefenseMapper.mapToSummaryDTOs(projectDefensesWithProjects));
                }
        );
        return projectDefenseDTOMap;
    }

    @Override
    @Transactional
    public void assignProjectToProjectDefense(String studyYear, String indexNumber, Long projectDefenseId, ProjectDefensePatchDTO projectDefensePatchDTO) {
        ProjectDefense projectDefense = projectDefenseDAO.findById(projectDefenseId).orElseThrow(() ->
                new BusinessException(MessageFormat.format("Project defense with id: {0} not found", projectDefenseId)));

        Project previouslyAssignedProject = projectDefense.getProject();

        if ((Objects.isNull(previouslyAssignedProject) && Objects.isNull(projectDefensePatchDTO.projectId()))
                || (Objects.nonNull(previouslyAssignedProject) && Objects.nonNull(projectDefensePatchDTO.projectId()) && Objects.equals(previouslyAssignedProject.getId(), projectDefensePatchDTO.projectId()))) {
            log.info("Update for project defense with id: {} was skipped - values from database and from request are the same", projectDefenseId);
            return;
        }

        DefensePhase defensePhase = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear).getDefensePhase();

        if (projectMemberService.isUserRoleCoordinator(indexNumber)) {
            assignProjectToProjectDefenseAsCoordinator(projectDefensePatchDTO, previouslyAssignedProject, projectDefense);
        }
        if (isUserAProjectAdminAndDefensePhaseAllowTheModifications(indexNumber, defensePhase)) {
            assignProjectToProjectDefenseAsProjectAdmin(indexNumber, projectDefensePatchDTO, previouslyAssignedProject, projectDefense);
        }
    }

    @Override
    public List<ProjectNameDTO> getProjectNames(String studyYear) {
        List<Tuple> projectsWithDefenseInfoForStudyYear = projectDAO.findAcceptedProjectsWithDefenseInfoForStudyYear(studyYear);
        return projectsWithDefenseInfoForStudyYear.stream()
                .map(this::mapTupleToProjectNameDto)
                .toList();
    }

    private ProjectNameDTO mapTupleToProjectNameDto(Tuple tuple) {
        Project project = (Project) tuple.get("project");
        Long defenseId = (Long) tuple.get("projectDefenseId");
        return new ProjectNameDTO(project.getId(), project.getName(), defenseId);
    }

    private boolean isUserAProjectAdminAndDefensePhaseAllowTheModifications(String indexNumber, DefensePhase defensePhase) {
        return Objects.equals(UserRole.PROJECT_ADMIN, projectMemberService.getUserRoleByUserIndex(indexNumber, UserRoleType.SPECIAL))
                && Objects.equals(DefensePhase.DEFENSE_PROJECT_REGISTRATION, defensePhase);
    }

    private void assignProjectToProjectDefenseAsProjectAdmin(String indexNumber, ProjectDefensePatchDTO projectDefensePatchDTO, Project previouslyAssignedProject, ProjectDefense projectDefense) {
        if (Objects.nonNull(previouslyAssignedProject) && !projectMemberService.isStudentAnAdminOfTheProject(indexNumber, previouslyAssignedProject.getId())) {
            return;
        }
        Project newlyAssignedProject = null;
        if (Objects.nonNull(projectDefensePatchDTO.projectId())) {
            newlyAssignedProject = getProjectById(projectDefensePatchDTO.projectId());
        }
        if (Objects.isNull(newlyAssignedProject)) {
            if (Objects.nonNull(previouslyAssignedProject)) {
                projectDefense.setProject(null);
                projectDefenseDAO.save(projectDefense);
            }
        } else if (permissionService.isProjectDefenseEditableForProjectAdmin(projectDefense, indexNumber, newlyAssignedProject)) {
            removeExistingProjectDefenseAssignments(projectDefensePatchDTO);
            projectDefense.setProject(newlyAssignedProject);
            projectDefenseDAO.save(projectDefense);
        }
    }

    private Project getProjectById(Long projectId) {
        return projectDAO.findById(projectId).orElseThrow(() ->
                new BusinessException(MessageFormat.format("Project id: {0} not found", projectId)));
    }

    private void assignProjectToProjectDefenseAsCoordinator(ProjectDefensePatchDTO projectDefensePatchDTO, Project previouslyAssignedProject, ProjectDefense projectDefense) {
        // TODO: 12/9/2023 should the restriction, to not allow assign project to time slot where supervisor is not a committe member, be implemented? 
        if (Objects.nonNull(projectDefensePatchDTO.projectId())) {
            removeExistingProjectDefenseAssignments(projectDefensePatchDTO);
        }
        if (Objects.isNull(projectDefensePatchDTO.projectId())) {
            if (Objects.nonNull(previouslyAssignedProject)) {
                defenseNotificationService.notifyStudentsAboutProjectDefenseAssignment(new ArrayList<>(previouslyAssignedProject.getStudents()));
            }
        } else {
            Project newlyAssignedProject = getProjectById(projectDefensePatchDTO.projectId());
            projectDefense.setProject(newlyAssignedProject);
            projectDefenseDAO.save(projectDefense);
            defenseNotificationService.notifyStudentsAboutProjectDefenseAssignment(new ArrayList<>(newlyAssignedProject.getStudents()));
            if (Objects.nonNull(previouslyAssignedProject)) {
                defenseNotificationService.notifyStudentsAboutProjectDefenseAssignment(new ArrayList<>(previouslyAssignedProject.getStudents()));
            }
        }
    }

    private void removeExistingProjectDefenseAssignments(ProjectDefensePatchDTO projectDefensePatchDTO) {
        List<ProjectDefense> projectDefensesConnectedWithPreviousProject = projectDefenseDAO.findAllByProjectId(projectDefensePatchDTO.projectId());
        projectDefensesConnectedWithPreviousProject.forEach(defense -> {
            defense.setProject(null);
            projectDefenseDAO.save(defense);
        });
    }

    private Map<String, List<ProjectDefenseDTO>> createProjectDefenseDTOMap(String studyYear, String indexNumber, List<ProjectDefense> projectDefenses) {
        Map<LocalDate, List<ProjectDefense>> projectDefenseMap = mapProjectDefenseByDate(projectDefenses);

        UserRole userRole = projectMemberService.getUserRoleByUserIndex(indexNumber, UserRoleType.SPECIAL);
        Project projectAdminProject = Objects.equals(UserRole.PROJECT_ADMIN, userRole) ? projectDAO.findByProjectAdmin(indexNumber, studyYear) : null;
        DefensePhase defensePhase = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear).getDefensePhase();

        Map<String, List<ProjectDefenseDTO>> projectDefenseDTOMap = new TreeMap<>();

        projectDefenseMap.forEach((date, defenses) -> {
            List<ProjectDefenseDTO> projectDefenseDTOs = mapProjectDefensesToDTOs(defenses, indexNumber, projectAdminProject, userRole, defensePhase);
            projectDefenseDTOMap.put(date.format(dateTimeFormatter), projectDefenseDTOs);
        });
        return projectDefenseDTOMap;
    }

    private Map<LocalDate, List<ProjectDefense>> mapProjectDefenseByDate(List<ProjectDefense> projectDefenses) {
        return projectDefenses.stream()
                .collect(Collectors.groupingBy(projectDefense -> projectDefense.getDefenseTimeslot().getDate()));
    }

    /**
     * Maps project defense entities to dto list and set the value of a field isEditable for every {@link ProjectDefenseDTO} object based on the user role,
     * the project defense process phase and project attributes
     * - for user with role COORDINATOR all project defenses are editable
     * - for users with role SUPERVISOR or STUDENT - none project defenses are editable (no mapping needed as isEditable is set to false by default)
     * - for user with role PROJECT_ADMIN project is editable if the project defense phase is equal to DEFENSE_PROJECT_REGISTRATION, the supervisor
     * of the project is a member of the committee and the time slot is free (or used by a project of a user)
     *
     * @param defenses     - project defenses entities to be mapped
     * @param indexNumber  - index number of the user
     * @param project      - project of the user (not null only for a user with a role PROJECT_ADMIN)
     * @param userRole     - the highest role of the user
     * @param defensePhase - the phase of the project defense process
     * @return mapped and sorted list of objects {@link ProjectDefenseDTO}
     */
    private List<ProjectDefenseDTO> mapProjectDefensesToDTOs(List<ProjectDefense> defenses, String indexNumber,
                                                             Project project, UserRole userRole, DefensePhase defensePhase) {
        return switch (userRole) {
            case COORDINATOR -> {
                List<ProjectDefenseDTO> projectDefenseDTOs = mapProjectDefensesToDTOsWithSorting(defenses);
                projectDefenseDTOs.forEach(projectDefense -> projectDefense.setEditable(true));
                yield projectDefenseDTOs;
            }
            case PROJECT_ADMIN -> {
                if (Objects.equals(DefensePhase.DEFENSE_PROJECT_REGISTRATION, defensePhase)) {
                    List<ProjectDefenseDTO> projectDefenseDTOs = mapProjectDefensesToDTOsForProjectAdmin(defenses, indexNumber, project);
                    yield projectDefenseDTOs.stream()
                            .sorted(projectDefenseByTimeComparator())
                            .toList();
                } else {
                    yield mapProjectDefensesToDTOsWithSorting(defenses);
                }
            }
            case STUDENT, SUPERVISOR -> mapProjectDefensesToDTOsWithSorting(defenses);
        };
    }

    /**
     * Maps project defense entities to dto list and set the value of a field isEditable for every {@link ProjectDefenseDTO} object for a user with
     * a PROJECT_ADMIN role. The value of isEditable is set to true if the project defense phase is equal to DEFENSE_PROJECT_REGISTRATION AND the supervisor
     * of the project is a member of the committee and the time slot is free (or used by a project of a user)
     *
     * @param defenses    - project defenses entities to be mapped
     * @param indexNumber - index number of a user with PROJECT_ADMIN role
     * @param project     - project of a user with PROJECT_ADMIN role
     * @return mapped list of objects {@link ProjectDefenseDTO}
     */
    private List<ProjectDefenseDTO> mapProjectDefensesToDTOsForProjectAdmin(List<ProjectDefense> defenses, String indexNumber, Project project) {
        List<ProjectDefenseDTO> projectDefenseDTOs = new ArrayList<>();
        defenses.forEach(projectDefense -> {
            boolean isEditable = permissionService.isProjectDefenseEditableForProjectAdmin(projectDefense, indexNumber, project);
            ProjectDefenseDTO projectDefenseDTO = projectDefenseMapper.mapToDto(projectDefense);
            projectDefenseDTO.setEditable(isEditable);
            projectDefenseDTOs.add(projectDefenseDTO);
        });
        return projectDefenseDTOs;
    }

    /**
     * Maps a list of {@link ProjectDefense entities} to list of {@link ProjectDefenseDTO} with sorting by time
     *
     * @param defenses
     * @return
     */
    private List<ProjectDefenseDTO> mapProjectDefensesToDTOsWithSorting(List<ProjectDefense> defenses) {
        return projectDefenseMapper.mapToDTOs(defenses).stream()
                .sorted(projectDefenseByTimeComparator())
                .toList();
    }

    private Comparator<ProjectDefenseDTO> projectDefenseByTimeComparator() {
        return Comparator.comparing(ProjectDefenseDTO::getTime);
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
