package pl.edu.amu.wmi.service.project.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.*;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.Semester;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.mapper.project.ProjectMapper;
import pl.edu.amu.wmi.mapper.project.StudentProjectMapper;
import pl.edu.amu.wmi.model.UserRoleType;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;
import pl.edu.amu.wmi.model.project.StudentDTO;
import pl.edu.amu.wmi.service.PermissionService;
import pl.edu.amu.wmi.service.ProjectMemberService;
import pl.edu.amu.wmi.service.externallink.ExternalLinkService;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;
import pl.edu.amu.wmi.service.project.ProjectService;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.*;
import static pl.edu.amu.wmi.enumerations.EvaluationPhase.DEFENSE_PHASE;
import static pl.edu.amu.wmi.enumerations.EvaluationPhase.SEMESTER_PHASE;
import static pl.edu.amu.wmi.enumerations.EvaluationStatus.*;
import static pl.edu.amu.wmi.enumerations.UserRole.*;


@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    public static final Integer NUMBER_OF_EVALUATION_CARDS_IN_SINGLE_SEMESTER = 3;

    private final ProjectDAO projectDAO;

    private final StudentDAO studentDAO;

    private final SupervisorDAO supervisorDAO;

    private final StudyYearDAO studyYearDAO;

    private final StudentProjectDAO studentProjectDAO;

    private final RoleDAO roleDAO;

    private final ProjectDefenseDAO projectDefenseDAO;

    private final ProjectMapper projectMapper;

    private final StudentProjectMapper studentMapper;

    private final EvaluationCardService evaluationCardService;

    private final ExternalLinkService externalLinkService;
    private final ProjectMemberService projectMemberService;
    private final PermissionService permissionService;

    @Autowired
    public ProjectServiceImpl(ProjectDAO projectDAO,
                              StudentDAO studentDAO,
                              SupervisorDAO supervisorDAO,
                              StudyYearDAO studyYearDAO,
                              StudentProjectDAO studentProjectDAO,
                              RoleDAO roleDAO,
                              ProjectDefenseDAO projectDefenseDAO,
                              ProjectMapper projectMapper,
                              StudentProjectMapper studentMapper,
                              EvaluationCardService evaluationCardService,
                              ExternalLinkService externalLinkService,
                              ProjectMemberService projectMemberService,
                              PermissionService permissionService) {
        this.projectDAO = projectDAO;
        this.studentDAO = studentDAO;
        this.supervisorDAO = supervisorDAO;
        this.studyYearDAO = studyYearDAO;
        this.studentProjectDAO = studentProjectDAO;
        this.roleDAO = roleDAO;
        this.projectDefenseDAO = projectDefenseDAO;
        this.externalLinkService = externalLinkService;
        this.projectMapper = projectMapper;
        this.studentMapper = studentMapper;
        this.evaluationCardService = evaluationCardService;
        this.projectMemberService = projectMemberService;
        this.permissionService = permissionService;
    }

    @Override
    public ProjectDetailsDTO findByIdWithRestrictions(String studyYear, String userIndexNumber, Long id) {
        Project project = projectDAO.findById(id).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", id)));

        List<StudentDTO> studentDTOs = prepareStudentDTOs(project);
        ProjectDetailsDTO projectDetailsDTO;
        if (permissionService.isUserAllowedToSeeProjectDetails(studyYear, userIndexNumber, project.getId())) {
            // TODO: 11/25/2023 what with external links in defense / retake phase ??
            projectDetailsDTO = projectMapper.mapToProjectDetailsDto(project);
            EvaluationCard activeEvaluationCard = project.getEvaluationCards().stream()
                    .filter(evaluationCard -> Objects.equals(Boolean.TRUE, evaluationCard.isActive()))
                    .findFirst().orElseThrow(()
                            -> new BusinessException(MessageFormat.format("There is none active evaluation card for project with id: {0}", id)));
            if (shouldfreezeButtonBeShown(activeEvaluationCard)) {
                projectDetailsDTO.setFreezeButtonShown(true);
            } else if (shouldPublishButtonBeShown(activeEvaluationCard)) {
                projectDetailsDTO.setPublishButtonShown(true);
            } else if (shouldRetakeButtonBeShown(activeEvaluationCard)) {
                projectDetailsDTO.setRetakeButtonShown(true);
            }
        } else {
            projectDetailsDTO = projectMapper.mapToProjectDetailsWithRestrictionsDto(project);
        }

        projectDetailsDTO.setStudents(studentDTOs);
        projectDetailsDTO.setAdmin(getIndexNumberOfProjectAdmin(project));
        return projectDetailsDTO;
    }

    private boolean shouldRetakeButtonBeShown(EvaluationCard activeEvaluationCard) {
        return Objects.equals(DEFENSE_PHASE, activeEvaluationCard.getEvaluationPhase())
                && Objects.equals(PUBLISHED, activeEvaluationCard.getEvaluationStatus());
    }

    private boolean shouldPublishButtonBeShown(EvaluationCard activeEvaluationCard) {
        return Objects.equals(DEFENSE_PHASE, activeEvaluationCard.getEvaluationPhase())
                && Objects.equals(ACTIVE, activeEvaluationCard.getEvaluationStatus());
    }

    private boolean shouldfreezeButtonBeShown(EvaluationCard activeEvaluationCard) {
        return Objects.equals(SEMESTER_PHASE, activeEvaluationCard.getEvaluationPhase())
                && Objects.equals(ACTIVE, activeEvaluationCard.getEvaluationStatus());
    }

    private List<StudentDTO> prepareStudentDTOs(Project project) {
        return project.getAssignedStudents().stream().map(this::prepareStudentDTO).toList();
    }

    private StudentDTO prepareStudentDTO(StudentProject studentProject) {
        StudentDTO studentDTO = studentMapper.mapToDto(studentProject.getStudent());
        studentDTO.setRole(studentProject.getProjectRole());
        studentDTO.setAccepted(studentProject.isProjectConfirmed());
        return studentDTO;
    }

    private String getIndexNumberOfProjectAdmin(Project project) {
        Student projectAdmin = getStudentProjectOfAdmin(project).getStudent();
        return projectAdmin.getIndexNumber();
    }

    @Override
    public List<ProjectDTO> findAllWithSortingAndRestrictions(String studyYear, String userIndexNumber) {
        List<Project> projectEntityList = projectDAO.findAllByStudyYear_StudyYear(studyYear);
        Student student = studentDAO.findByStudyYearAndUserData_IndexNumber(studyYear, userIndexNumber);
        Supervisor supervisor = supervisorDAO.findByStudyYearAndUserData_IndexNumber(studyYear, userIndexNumber);

        if (student != null) {
            List<Long> studentProjectsIds = getStudentProjectsIds(student);
            Comparator<Project> byStudentAssignedAndConfirmedProjects =
                    createComparatorByStudentAssignedAndConfirmedProjects(studentProjectsIds, student);

            return prepareSortedProjectListWithRestrictions(projectEntityList, studentProjectsIds, byStudentAssignedAndConfirmedProjects, false, false);
        } else {
            List<Long> supervisorProjectIds = getSupervisorProjectIds(supervisor);
            Comparator<Project> bySupervisorAssignedAndAcceptedProjects = createComparatorBySupervisorAssignedAndAcceptedProjects(supervisor);

            if (projectMemberService.isUserRoleCoordinator(userIndexNumber)) {
                return prepareSortedProjectListWithRestrictions(projectEntityList, supervisorProjectIds, bySupervisorAssignedAndAcceptedProjects, false, true);
            }
            return prepareSortedProjectListWithRestrictions(projectEntityList, supervisorProjectIds, bySupervisorAssignedAndAcceptedProjects, true, false);
        }
    }

    private Comparator<Project> createComparatorByStudentAssignedAndConfirmedProjects(List<Long> studentProjectsIds, Student student) {
        return Comparator
                .comparing((Project p) -> !studentProjectsIds.contains(p.getId()))
                .thenComparing((Project p) -> !isProjectEqualToStudentsConfirmed(p, student));
    }

    private List<Long> getStudentProjectsIds(Student student) {
        if (Objects.isNull(student))
            return new ArrayList<>();
        return student.getAssignedProjects().stream()
                .map(sp -> sp.getProject().getId()).toList();
    }

    private Comparator<Project> createComparatorBySupervisorAssignedAndAcceptedProjects(Supervisor supervisor) {
        return Comparator
                .comparing((Project p) -> !p.getSupervisor().equals(supervisor))
                .thenComparing((Project p) -> !p.getAcceptanceStatus().equals(ACCEPTED));
    }

    private List<Long> getSupervisorProjectIds(Supervisor supervisor) {
        if (Objects.isNull(supervisor))
            return new ArrayList<>();
        return supervisor.getProjects().stream()
                .map(BaseAbstractEntity::getId)
                .toList();
    }

    private List<ProjectDTO> prepareSortedProjectListWithRestrictions(List<Project> projects, List<Long> userProjectIds,
                                                                      Comparator<Project> comparator, boolean isSupervisor,
                                                                      boolean isCoordinator) {
        projects.sort(comparator);
        List<ProjectDTO> projectDTOs = new ArrayList<>();
        projects.forEach(project -> {
            if ((userProjectIds.contains(project.getId()) && !isEvaluationCardFreeze(project)) || isCoordinator) {
                projectDTOs.add(mapToProjectDTO(project, MappingMode.FULL));
            } else if (userProjectIds.contains(project.getId()) && isEvaluationCardFreeze(project)) {
                projectDTOs.add(mapToProjectDTO(project, MappingMode.FULL_WITHOUT_GRADES));
            } else if (isSupervisor && isProjectInDefenseOrRetakePhase(project)) {
                projectDTOs.add(mapToProjectDTO(project, MappingMode.WITH_PARTIAL_RESTRICTIONS));
            } else {
                projectDTOs.add(mapToProjectDTO(project, MappingMode.WITH_FULL_RESTRICTIONS));
            }
        });
        return projectDTOs;
    }

    private boolean isEvaluationCardFreeze(Project project) {
        return project.getEvaluationCards().stream()
                .anyMatch(evaluationCard -> Objects.equals(FROZEN, evaluationCard.getEvaluationStatus()));
    }

    private ProjectDTO mapToProjectDTO(Project entity, MappingMode mode) {
        switch (mode) {
            case FULL -> {
                ProjectDTO projectDTO = projectMapper.mapToProjectDto(entity);
                return fillProjectDtoWithNecessaryData(projectDTO, entity);
            }
            case FULL_WITHOUT_GRADES -> {
                ProjectDTO projectDTO = projectMapper.mapToProjectDto(entity);
                ProjectDTO updatedProjectDTO = fillProjectDtoWithNecessaryData(projectDTO, entity);
                updatedProjectDTO.setPointsSecondSemester(null);
                updatedProjectDTO.setPointsFirstSemester(null);
                return updatedProjectDTO;
            }
            case WITH_PARTIAL_RESTRICTIONS -> {
                ProjectDTO projectDTO = projectMapper.mapToProjectDtoWithRestrictionsInPhaseDefense(entity);
                return fillProjectDtoWithNecessaryData(projectDTO, entity);
            }
            case WITH_FULL_RESTRICTIONS -> {
                ProjectDTO projectDTO = projectMapper.mapToProjectDtoWithRestrictions(entity);
                projectDTO.setStudents(entity.getStudentsBasicData());
                return projectDTO;
            }
            default -> throw new IllegalArgumentException("Unknown mapping mode: " + mode);
        }
    }

    private ProjectDTO fillProjectDtoWithNecessaryData(ProjectDTO projectDTO, Project entity) {
        projectDTO.setPointsFirstSemester(evaluationCardService.getPointsForSemester(entity, Semester.FIRST));
        projectDTO.setPointsSecondSemester(evaluationCardService.getPointsForSemester(entity, Semester.SECOND));
        projectDTO.setCriteriaMet(getCriteriaMet(entity));

        ProjectDefense projectDefense = projectDefenseDAO.findByProjectId(entity.getId());

        if (Objects.nonNull(projectDefense)) {
            projectDTO.setDefenseDay(projectDefense.getDefenseTimeslot().getDate());
            projectDTO.setDefenseTime(projectDefense.getDefenseTimeslot().getStartTime());
            projectDTO.setClassroom(projectDefense.getClassroom());
            projectDTO.setCommittee(projectDefense.getCommitteeInitials());
        }

        projectDTO.setEvaluationPhase(getDetailedEvaluationPhase(entity));
        projectDTO.setStudents(entity.getStudentsBasicData());

        return projectDTO;
    }

    private String getDetailedEvaluationPhase(Project project) {
        EvaluationCard theMostRecentEvaluationCard = evaluationCardService.findTheMostRecentEvaluationCard(project.getEvaluationCards(), null)
                .orElseThrow(() ->  new BusinessException(MessageFormat.format("The most recent evaluation card was not found for project with id: {0}", project.getId())));

        EvaluationPhase evaluationPhase = theMostRecentEvaluationCard.getEvaluationPhase();
        EvaluationStatus evaluationStatus = theMostRecentEvaluationCard.getEvaluationStatus();

        if (Objects.equals(evaluationPhase, SEMESTER_PHASE))
            return ACTIVE.label;
        else if (Objects.equals(evaluationPhase, DEFENSE_PHASE) && !Objects.equals(evaluationStatus, PUBLISHED))
            return FROZEN.label;
        else if (Objects.equals(evaluationPhase, DEFENSE_PHASE) && Objects.equals(evaluationStatus, PUBLISHED))
            return PUBLISHED.label;
        else
            return RETAKE.label;
    }

    private boolean getCriteriaMet(Project entity) {
        Optional<EvaluationCard> theMostRecentEvaluationCard = evaluationCardService.findTheMostRecentEvaluationCard(entity.getEvaluationCards(), null);
        return theMostRecentEvaluationCard.map(evaluationCard -> !evaluationCard.isDisqualified()).orElse(false);
    }

    private boolean isProjectInDefenseOrRetakePhase(Project project) {
        Predicate<EvaluationCard> isDefensePhase = evaluationCard -> Objects.equals(EvaluationPhase.DEFENSE_PHASE, evaluationCard.getEvaluationPhase());
        Predicate<EvaluationCard> isRetakePhase = evaluationCard -> Objects.equals(EvaluationPhase.RETAKE_PHASE, evaluationCard.getEvaluationPhase());
        return project.getEvaluationCards().stream()
                .anyMatch(isDefensePhase.or(isRetakePhase));
    }

    private boolean isProjectEqualToStudentsConfirmed(Project project, Student student) {
        Project studentConfirmedProject = student.getConfirmedProject();
        if (studentConfirmedProject != null) {
            return studentConfirmedProject.equals(project);
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public ProjectDetailsDTO saveProject(ProjectDetailsDTO projectDTO, String studyYear, String userIndexNumber) {
        Project projectEntity = projectMapper.mapToEntity(projectDTO);
        Supervisor supervisorEntity = supervisorDAO.findByStudyYearAndUserData_IndexNumber(studyYear, projectDTO.getSupervisor().getIndexNumber());
        StudyYear studyYearEntity = studyYearDAO.findByStudyYear(studyYear);

        String adminIndexNumber = projectDTO.getAdmin();
        List<StudentDTO> studentDTOs = projectDTO.getStudents();
        addStudentsToProject(projectEntity, studyYear, adminIndexNumber, studentDTOs);

        projectEntity.setSupervisor(supervisorEntity);
        projectEntity.setStudyYear(studyYearEntity);
        projectEntity.setAcceptanceStatus(acceptanceStatusByStudentsAmount(projectDTO));
        projectEntity.setExternalLinks(externalLinkService.createEmptyExternalLinks(studyYear));

        addEvaluationCardToProject(projectEntity, studyYear, Semester.FIRST);
        projectEntity = projectDAO.save(projectEntity);

        addEvaluationCardToProject(projectEntity, studyYear, Semester.SECOND);
        projectEntity = projectDAO.save(projectEntity);

        return projectMapper.mapToProjectDetailsDto(projectEntity);
    }

    private void addStudentsToProject(Project project, String studyYear, String adminIndexNumber, List<StudentDTO> students) {
        for (StudentDTO student : students) {
            Student studentEntity = studentDAO.findByStudyYearAndUserData_IndexNumber(studyYear, student.getIndexNumber());
            if (isProjectAdmin(studentEntity, adminIndexNumber)) {
                setStudentToBeAdmin(studentEntity);
            }
            project.addStudent(studentEntity, student.getRole(), isProjectAdmin(studentEntity, adminIndexNumber));
        }
    }

    private void setStudentToBeAdmin(Student student) {
        student.setProjectAdmin(true);
        student.getUserData().getRoles().add(roleDAO.findByName(PROJECT_ADMIN));
    }

    private void addEvaluationCardToProject(Project project, String studyYear, Semester semester) {
        EvaluationCard evaluationCard = new EvaluationCard();
        project.addEvaluationCard(evaluationCard);
        evaluationCard.setProject(project);

        switch (semester) {
            case FIRST -> evaluationCardService.createEvaluationCard(project, studyYear,
                    Semester.FIRST, EvaluationPhase.SEMESTER_PHASE, EvaluationStatus.ACTIVE, Boolean.TRUE);
            case SECOND -> evaluationCardService.createEvaluationCard(project, studyYear,
                    Semester.SECOND, EvaluationPhase.SEMESTER_PHASE, INACTIVE, Boolean.FALSE);
        }

    }

    @Override
    @Transactional
    public ProjectDetailsDTO updateProject(String studyYear, String userIndexNumber, Long projectId, ProjectDetailsDTO projectDetailsDTO) {
        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project not found: {0}", projectId)));
        Supervisor supervisorEntity = supervisorDAO.findByStudyYearAndUserData_IndexNumber(studyYear, projectDetailsDTO.getSupervisor().getIndexNumber());
        StudyYear studyYearEntity = studyYearDAO.findByStudyYear(studyYear);
        projectEntity.setSupervisor(supervisorEntity);
        projectEntity.setStudyYear(studyYearEntity);
        projectEntity.setName(projectDetailsDTO.getName());
        projectEntity.setDescription(projectDetailsDTO.getDescription());
        projectEntity.setTechnologies(projectDetailsDTO.getTechnologies());
        externalLinkService.updateExternalLinks(projectDetailsDTO.getExternalLinks());

        Set<StudentProject> currentAssignedStudents = projectEntity.getAssignedStudents();
        Set<StudentProject> newAssignedStudents = getAssignedStudentsToUpdate(projectDetailsDTO);
        Set<StudentProject> assignedStudentsToRemove = getAssignedStudentsToRemove(currentAssignedStudents, newAssignedStudents);

        // TODO: 6/23/2023 Extract students removing to a new method
        if (projectMemberService.isUserRoleCoordinator(userIndexNumber)) {
            assignedStudentsToRemove.forEach(assignedStudent -> {
                Long studentId = assignedStudent.getStudent().getId();
                Student student = studentDAO.findById(studentId).orElseThrow(()
                        -> new ProjectManagementException(MessageFormat.format("Student not found: {0}", studentId)));
                if (Objects.nonNull(student.getConfirmedProject()) && Objects.equals(student.getConfirmedProject().getId(), projectEntity.getId())) {
                    if (student.isProjectAdmin()) {
                        removeAdminRoleFromStudent(student);
                        student.setProjectAdmin(false);
                    }
                    student.setConfirmedProject(null);
                    student.setProjectConfirmed(false);
                    student.setProjectRole(null);
                    studentDAO.save(student);
                }
            });
            // TODO: 6/23/2023 Temporary workaround, need to be handled in a different way
            projectDetailsDTO.setAdmin(projectDetailsDTO.getStudents().get(0).getIndexNumber());
        } else {
            assignedStudentsToRemove.forEach(assignedStudent -> {
                Long studentId = assignedStudent.getStudent().getId();
                Student student = studentDAO.findById(studentId).orElseThrow(()
                        -> new ProjectManagementException(MessageFormat.format("Student not found: {0}", studentId)));
                if (Objects.nonNull(student.getConfirmedProject()) && Objects.equals(student.getConfirmedProject().getId(), projectEntity.getId())) {
                    student.setConfirmedProject(null);
                    student.setProjectConfirmed(false);
                    student.setProjectRole(null);
                    studentDAO.save(student);
                }
            });
        }

        // TODO: 6/23/2023 refactor needed
        updateProjectAdmin(projectId, projectDetailsDTO.getAdmin());

        studentProjectDAO.deleteAll(assignedStudentsToRemove);
        projectEntity.removeStudentProject(assignedStudentsToRemove);

        updateCurrentStudentRole(projectEntity.getAssignedStudents(), projectDetailsDTO.getStudents());

        for (StudentDTO student : projectDetailsDTO.getStudents()) {
            // TODO: exception handling the student not found | add second param- study year to serach (after data-feed adjustments)
            Student entity = studentDAO.findByUserData_IndexNumber(student.getIndexNumber());
            if (!entity.getIndexNumber().equals(userIndexNumber))
                projectEntity.addStudent(entity, student.getRole(), false);
        }

        if (projectMemberService.isUserRoleCoordinator(userIndexNumber)) {
            projectEntity.getAssignedStudents().forEach(assignedStudent -> {
                assignedStudent.setProjectConfirmed(true);
                assignedStudent.getStudent().setProjectConfirmed(true);
                assignedStudent.getStudent().setConfirmedProject(projectEntity);
            });
        }

        updateProjectStatus(projectEntity);

        projectDAO.save(projectEntity);
        return projectMapper.mapToProjectDetailsDto(projectEntity);
    }

    private void updateProjectStatus(Project project) {
        boolean isProjectNotConfirmed = project.getAssignedStudents().stream()
                .anyMatch(s -> Objects.equals(Boolean.FALSE, s.isProjectConfirmed()));
        if (isProjectNotConfirmed && project.getAcceptanceStatus() != PENDING) {
            project.setAcceptanceStatus(PENDING);
        } else if (!isProjectNotConfirmed && project.getAcceptanceStatus() == PENDING) {
            project.setAcceptanceStatus(CONFIRMED);
        }
    }

    private void updateCurrentStudentRole(Set<StudentProject> assignedStudents, List<StudentDTO> students) {
        for (StudentProject studentProject : assignedStudents) {
            students.stream()
                    .filter(s -> Objects.equals(s.getIndexNumber(), studentProject.getStudent().getIndexNumber()))
                    .map(StudentDTO::getRole)
                    .findFirst().ifPresent(studentProject::setProjectRole);
        }
    }

    @Override
    @Transactional
    public ProjectDetailsDTO updateProjectAdmin(Long projectId, String studentIndex) {

        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));
        Role role = roleDAO.findByName(PROJECT_ADMIN);

        StudentProject currentAdminStudentProjectEntity = getStudentProjectOfAdmin(projectEntity);
        currentAdminStudentProjectEntity.setProjectAdmin(false);
        studentProjectDAO.save(currentAdminStudentProjectEntity);

        StudentProject newAdminStudentProjectEntity = getStudentProjectByStudentIndex(projectEntity, studentIndex);
        newAdminStudentProjectEntity.setProjectAdmin(true);
        studentProjectDAO.save(newAdminStudentProjectEntity);

        Student currentAdminStudentEntity = currentAdminStudentProjectEntity.getStudent();
        Student newAdminStudentEntity = newAdminStudentProjectEntity.getStudent();

        currentAdminStudentEntity.setProjectAdmin(false);
        currentAdminStudentEntity.getUserData().getRoles().remove(role);
        newAdminStudentEntity.setProjectAdmin(true);
        newAdminStudentEntity.getUserData().getRoles().add(role);
        studentDAO.save(currentAdminStudentEntity);
        studentDAO.save(newAdminStudentEntity);

        ProjectDetailsDTO projectDetailsDTO = projectMapper.mapToProjectDetailsDto(projectEntity);
        projectDetailsDTO.setAdmin(newAdminStudentEntity.getIndexNumber());

        return projectDetailsDTO;

    }

    @Override
    @Transactional
    public ProjectDetailsDTO acceptProjectBySingleUser(String userIndexNumber, Long projectId) {

        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        if (projectMemberService.getUserRoleByUserIndex(userIndexNumber, UserRoleType.BASE).equals(STUDENT)) {
            StudentProject studentProjectEntity = getStudentProjectByStudentIndex(projectEntity, userIndexNumber);
            studentProjectEntity.setProjectConfirmed(true);
            studentProjectEntity.getStudent().setProjectConfirmed(true);
            studentProjectEntity.getStudent().setConfirmedProject(projectEntity);

            if (isProjectConfirmedByAllStudents(projectEntity)) {
                projectEntity.setAcceptanceStatus(CONFIRMED);
            }

            studentProjectDAO.save(studentProjectEntity);

        } else if (projectMemberService.getUserRoleByUserIndex(userIndexNumber, UserRoleType.BASE).equals(SUPERVISOR)) {
            log.info("Project with id: {} was accepted by all students and a supervisor", projectId);
            projectEntity.setAcceptanceStatus(ACCEPTED);
        } else {
            // TODO: 6/3/2023 handle wrong role exception
            log.error("Wrong user role - role must be a {} or {}.", STUDENT, SUPERVISOR);
        }

        projectDAO.save(projectEntity);

        return projectMapper.mapToProjectDetailsDto(projectEntity);
    }

    @Override
    @Transactional
    public ProjectDetailsDTO acceptProjectByAllStudents(Long projectId) {
        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        Set<StudentProject> studentProjectEntities = projectEntity.getAssignedStudents();

        studentProjectEntities.forEach(as -> {
            as.setProjectConfirmed(true);
            as.getStudent().setProjectConfirmed(true);
            as.getStudent().setConfirmedProject(projectEntity);
        });

        projectEntity.setAcceptanceStatus(CONFIRMED);

        studentProjectDAO.saveAll(studentProjectEntities);
        projectDAO.save(projectEntity);

        return projectMapper.mapToProjectDetailsDto(projectEntity);
    }

    @Override
    public ProjectDetailsDTO unAcceptProject(String studyYear, String userIndexNumber, Long projectId) {
        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        if (projectMemberService.getUserRoleByUserIndex(userIndexNumber, UserRoleType.BASE).equals(STUDENT)) {
            if (isProjectConfirmedByAllStudents(projectEntity)) {
                projectEntity.setAcceptanceStatus(PENDING);
            }
            StudentProject studentProjectEntity = getStudentProjectByStudentIndex(projectEntity, userIndexNumber);
            studentProjectEntity.setProjectConfirmed(false);
            studentProjectEntity.getStudent().setProjectConfirmed(false);
            studentProjectEntity.getStudent().setConfirmedProject(null);

            studentProjectDAO.save(studentProjectEntity);

        }
        return projectMapper.mapToProjectDetailsDto(projectEntity);
    }

    @Transactional
    @Override
    public void delete(Long projectId, String userIndexNumber) throws ProjectManagementException {
        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        if (!permissionService.validateDeletionPermission(userIndexNumber, projectEntity)) {
            log.error("Missing permission to delete project for user with index number: {}", userIndexNumber);
            throw new ProjectManagementException("Missing permission to delete project");
        }
        removeConfirmedProjectFromStudents(projectEntity);
        projectDAO.delete(projectEntity);
    }

    private void removeConfirmedProjectFromStudents(Project projectEntity) {
        Set<Student> students = projectEntity.getStudents();
        for (Student student : students) {
            if (Objects.nonNull(student.getConfirmedProject()) && Objects.equals(student.getConfirmedProject().getId(), projectEntity.getId())) {
                if (student.isProjectAdmin()) {
                    removeAdminRoleFromStudent(student);
                    student.setProjectAdmin(false);
                }
                student.setConfirmedProject(null);
                student.setProjectConfirmed(false);
                student.setProjectRole(null);
                studentDAO.save(student);
            }
        }
    }

    private void removeAdminRoleFromStudent(Student student) {
        student.getUserData().getRoles().remove(roleDAO.findByName(PROJECT_ADMIN));

    }

    private boolean isProjectConfirmedByAllStudents(Project project) {
        return project.getAssignedStudents().stream().allMatch(StudentProject::isProjectConfirmed);
    }

    private boolean isProjectAdmin(Student entity, String adminIndexNumber) {
        return Objects.equals(adminIndexNumber, entity.getIndexNumber());
    }

    private StudentProject getStudentProjectOfAdmin(Project project) {
        return project.getAssignedStudents().stream()
                .filter(StudentProject::isProjectAdmin)
                .findFirst().orElseThrow(()
                        -> new ProjectManagementException(MessageFormat.format("Admin of project with id: {0} not found", project.getId())));
    }

    private StudentProject getStudentProjectByStudentIndex(Project project, String index) {
        return project.getAssignedStudents().stream()
                .filter(studentProject -> isStudentProjectConnectedWithStudent(index, studentProject))
                .findFirst().orElseThrow(()
                        -> new ProjectManagementException(MessageFormat.format("Project with id: {0} is not connected with student: {1}",
                        project.getId(), index)));
    }

    private static boolean isStudentProjectConnectedWithStudent(String index, StudentProject studentProject) {
        return studentProject.getStudent().getIndexNumber().equals(index);
    }

    private AcceptanceStatus acceptanceStatusByStudentsAmount(ProjectDetailsDTO projectDetailsDTO) {
        if (projectDetailsDTO.getStudents().size() == 1)
            return CONFIRMED;
        else
            return PENDING;
    }

    private Set<StudentProject> getAssignedStudentsToUpdate(ProjectDetailsDTO projectDetailsDTO) {
        Set<StudentProject> studentProjectsForUpdate = new HashSet<>();
        projectDetailsDTO.getStudents().forEach(studentDTO -> {
            Student student = studentDAO.findByUserData_IndexNumber(studentDTO.getIndexNumber());
            StudentProject studentProject = studentProjectDAO.findByStudent_IdAndProject_Id(student.getId(), Long.valueOf(projectDetailsDTO.getId()));
            studentProjectsForUpdate.add(studentProject);
        });
        return studentProjectsForUpdate;
    }

    private Set<StudentProject> getAssignedStudentsToRemove(Set<StudentProject> currentAssignedStudents, Set<StudentProject> newAssignedStudents) {
        return currentAssignedStudents.stream()
                .filter(element -> !newAssignedStudents.contains(element))
                .collect(Collectors.toSet());
    }

    private enum MappingMode {
        FULL,
        FULL_WITHOUT_GRADES,
        WITH_PARTIAL_RESTRICTIONS,
        WITH_FULL_RESTRICTIONS
    }

}
