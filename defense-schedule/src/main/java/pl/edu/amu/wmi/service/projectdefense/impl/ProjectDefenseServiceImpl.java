package pl.edu.amu.wmi.service.projectdefense.impl;

import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.DefenseScheduleConfigDAO;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.dao.ProjectDefenseDAO;
import pl.edu.amu.wmi.entity.*;
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
import pl.edu.amu.wmi.util.CommonDateUtils;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectDefenseServiceImpl implements ProjectDefenseService {

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
    public void createProjectDefense(String studyYear, List<SupervisorDefenseAssignment> committeeMembers) {
        createNewProjectDefense(studyYear, committeeMembers);
    }

    @Override
    public List<ProjectDefenseDTO> getProjectDefenses(String studyYear, String indexNumber) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        if (Objects.isNull(defenseScheduleConfig)) {
            log.info("Defense schedule has been not configured yet for the study year {}", studyYear);
            return null;
        }
        DefensePhase defensePhase = defenseScheduleConfig.getDefensePhase();
        List<ProjectDefense> projectDefenses = projectDefenseDAO.findAllByStudyYearAndSupervisorDefenseAssignmentsNotEmpty(studyYear);
        return createProjectDefenseDTOs(studyYear, indexNumber, projectDefenses, defensePhase);
    }

    @Override
    public Map<String, List<ProjectDefenseSummaryDTO>> getProjectDefensesSummary(String studyYear) {
        List<ProjectDefense> projectDefenses = projectDefenseDAO.findAllByStudyYearAndSupervisorDefenseAssignmentsNotEmpty(studyYear);
        Map<LocalDate, List<ProjectDefense>> projectDefenseMap = projectDefenses.stream().collect(Collectors.groupingBy(projectDefense -> projectDefense.getDefenseTimeslot().getDate()));
        Map<String, List<ProjectDefenseSummaryDTO>> projectDefenseDTOMap = new TreeMap<>();
        projectDefenseMap.forEach((date, defenses) -> {
                    List<ProjectDefense> projectDefensesWithProjects = defenses.stream()
                            .filter(defense -> Objects.nonNull(defense.getProject()))
                            .toList();
                    projectDefenseDTOMap.put(CommonDateUtils.getDateStringWithTheDayOfWeek(date), projectDefenseMapper.mapToSummaryDTOs(projectDefensesWithProjects));
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
    @Transactional
    public Set<Project> assignProjectsToProjectDefenses(List<ProjectDefenseDTO> projectDefenseDTOs) {
        Set<Project> updatedProjects = new HashSet<>();
        projectDefenseDTOs.forEach(defense -> {
            Optional<Project> updatedProject = changeSingleAssignmentWhenNecessary(defense);
            updatedProject.ifPresent(updatedProjects::add);
        });
        return updatedProjects;
    }

    private Optional<Project> changeSingleAssignmentWhenNecessary(ProjectDefenseDTO projectDefenseDTO) {
        Long projectDefenseId = projectDefenseDTO.getProjectDefenseId();
        ProjectDefense projectDefenseEntity = projectDefenseDAO.findById(projectDefenseId)
                .orElseThrow(() -> new BusinessException(MessageFormat.format("Project defense with id: {0} not found", projectDefenseId)));

        Project projectDefenseProject = projectDefenseEntity.getProject();
        Long projectDefenseCurrentProjectId = Objects.nonNull(projectDefenseProject) ? projectDefenseProject.getId() : null;
        Long projectDefenseNewProjectId = Objects.nonNull(projectDefenseDTO.getProjectId()) ? Long.valueOf(projectDefenseDTO.getProjectId()) : null;
        boolean isProjectDefenseChanged = !Objects.equals(projectDefenseCurrentProjectId, projectDefenseNewProjectId);

        return updateProjectDefenseAssignment(isProjectDefenseChanged, projectDefenseNewProjectId, projectDefenseEntity);
    }

    private Optional<Project> updateProjectDefenseAssignment(boolean isProjectDefenseChanged, Long projectDefenseNewProjectId, ProjectDefense projectDefenseToUpdate) {
        Project updatedProject = null;

        if (isProjectDefenseChanged) {
            if (Objects.nonNull(projectDefenseNewProjectId)) {
                log.info("Trying to set project with id: {} for defense with id: {}", projectDefenseNewProjectId, projectDefenseToUpdate.getId());
                updatedProject = projectDAO.findById(projectDefenseNewProjectId).orElseThrow(() ->
                        new BusinessException(MessageFormat.format("Project with id: {0} not found", projectDefenseNewProjectId)));
                removeProjectFromExistingDefenseIfNeeded(projectDefenseNewProjectId);
                projectDefenseToUpdate.setProject(updatedProject);
            } else {
                log.info("Trying to remove project with id: {} for defense with id: {}", projectDefenseToUpdate.getProject().getId(), projectDefenseToUpdate.getId());
                projectDefenseToUpdate.setProject(null);
            }

            projectDefenseDAO.save(projectDefenseToUpdate);
            log.info("Project id: {} was successfully set for project defense with id: {}", projectDefenseNewProjectId, projectDefenseToUpdate.getId());
        }

        return Optional.ofNullable(updatedProject);
    }

    /**
     * Check if project from request has existing ProjectDefense entity related with it.
     * If yes remove project from ProjectDefense entity. Otherwise, do nothing.
     */
    private void removeProjectFromExistingDefenseIfNeeded(Long projectDefenseNewProjectId) {
        ProjectDefense projectDefenseToClear = projectDefenseDAO.findByProjectId(projectDefenseNewProjectId);
        if (Objects.nonNull(projectDefenseToClear)) {
            projectDefenseToClear.setProject(null);
            projectDefenseDAO.save(projectDefenseToClear);
            log.info("Project with id: {} was successfully removed from project defense with id: {}", projectDefenseNewProjectId, projectDefenseToClear.getId());
        }
    }

    @Override
    public List<ProjectNameDTO> getProjectNames(String studyYear) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findByStudyYearAndIsActiveIsTrue(studyYear);
        if (Objects.isNull(defenseScheduleConfig)) {
            log.info("Defense schedule has been not configured yet for the study year {}", studyYear);
            return null;
        }
        List<Tuple> projectsWithDefenseInfoForStudyYear = projectDAO.findAcceptedProjectsWithDefenseInfoForStudyYear(studyYear);
        return projectsWithDefenseInfoForStudyYear.stream()
                .map(this::mapTupleToProjectNameDto)
                .sorted(Comparator.comparing(ProjectNameDTO::getName))
                .toList();
    }

    @Override
    @Transactional
    public void deleteProjectDefenses(List<Long> projectDefenseIdsToBeRemoved) {
        projectDefenseIdsToBeRemoved.forEach(projectDefenseId -> {
            Optional<ProjectDefense> entity = projectDefenseDAO.findById(projectDefenseId);
            if (entity.isPresent()) {
                projectDefenseDAO.delete(entity.get());
                log.info("Project defense object with id: {} has been deleted", projectDefenseId);
            }
        });
    }

    private ProjectNameDTO mapTupleToProjectNameDto(Tuple tuple) {
        Project project = (Project) tuple.get("project");
        Long defenseId = (Long) tuple.get("projectDefenseId");
        String extendedProjectName = createExtendedProjectName(project);
        return new ProjectNameDTO(String.valueOf(project.getId()), extendedProjectName, defenseId);
    }

    private String createExtendedProjectName(Project project) {
        String supervisorInitials = project.getSupervisor().getInitials();
        return supervisorInitials + ": " + project.getName();
    }

    private boolean isUserAProjectAdminAndDefensePhaseAllowTheModifications(String indexNumber, DefensePhase defensePhase) {
        return Objects.equals(UserRole.PROJECT_ADMIN, projectMemberService.getUserRoleByUserIndex(indexNumber, UserRoleType.SPECIAL))
                && Objects.equals(DefensePhase.DEFENSE_PROJECT_REGISTRATION, defensePhase);
    }

    private void assignProjectToProjectDefenseAsProjectAdmin(String indexNumber, ProjectDefensePatchDTO projectDefensePatchDTO, Project previouslyAssignedProject, ProjectDefense projectDefense) {
        if (Objects.nonNull(previouslyAssignedProject) && !projectMemberService.isStudentAnAdminOfTheProject(indexNumber, previouslyAssignedProject.getId())) {
            log.info("Project defense slot {} cannot be updated by user: {}. Assignment of the project with id: {} was skipped",
                    indexNumber, projectDefense.getId(), projectDefensePatchDTO.projectId());
            return;
        }
        Project newlyAssignedProject = null;
        if (Objects.nonNull(projectDefensePatchDTO.projectId())) {
            newlyAssignedProject = getProjectById(projectDefensePatchDTO.projectId());
        }
        if (Objects.isNull(newlyAssignedProject)) {
            if (Objects.nonNull(previouslyAssignedProject)) {
                projectDefense.setProject(null);
                log.info("Project defense slot {} was updated by user: {}. Assignment of the project with id: {} was successful",
                        indexNumber, projectDefense.getId(), projectDefensePatchDTO.projectId());
                projectDefenseDAO.save(projectDefense);
            }
        } else if (permissionService.isProjectDefenseEditableForProjectAdmin(projectDefense, indexNumber, newlyAssignedProject)) {
            removeExistingProjectDefenseAssignments(projectDefensePatchDTO);
            projectDefense.setProject(newlyAssignedProject);
            log.info("Project defense slot {} was updated by user: {}. Assignment of the project with id: {} was successful",
                    indexNumber, projectDefense.getId(), projectDefensePatchDTO.projectId());
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

    private List<ProjectDefenseDTO> createProjectDefenseDTOs(String studyYear, String indexNumber, List<ProjectDefense> projectDefenses, DefensePhase defensePhase) {
        UserRole userRole = projectMemberService.getUserRoleByUserIndex(indexNumber, UserRoleType.SPECIAL);
        Project projectAdminProject = Objects.equals(UserRole.PROJECT_ADMIN, userRole) ? projectDAO.findByProjectAdmin(indexNumber, studyYear) : null;

        return mapProjectDefensesToDTOs(projectDefenses, indexNumber, projectAdminProject, userRole, defensePhase);
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
                } else if (Objects.equals(DefensePhase.SCHEDULE_PLANNING, defensePhase)) {
                    yield Collections.emptyList();
                } else {
                    yield mapProjectDefensesToDTOsWithSorting(defenses);
                }
            }
            case STUDENT -> {
                if (Objects.equals(DefensePhase.SCHEDULE_PLANNING, defensePhase)) {
                    yield Collections.emptyList();
                } else {
                    yield mapProjectDefensesToDTOsWithSorting(defenses);
                }
            }
            case SUPERVISOR -> mapProjectDefensesToDTOsWithSorting(defenses);
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
        return Comparator.comparing(ProjectDefenseDTO::getDate).thenComparing(ProjectDefenseDTO::getTime);
    }

    private void createNewProjectDefense(String studyYear, List<SupervisorDefenseAssignment> supervisorDefenseAssignments) {
        ProjectDefense projectDefense = new ProjectDefense();
        projectDefense.addSupervisorDefenseAssignments(supervisorDefenseAssignments);
        projectDefense.setStudyYear(studyYear);
        projectDefense = projectDefenseDAO.save(projectDefense);
        log.info("Project Defense with id: {} has been created", projectDefense.getId());
    }

    @Override
    public List<Student> getStudentsFromProjectDefenses(Set<Project> projects) {
        List<Student> students = new ArrayList<>();
        projects.forEach(project -> students.addAll(project.getStudents()));
        return students;
    }

}
