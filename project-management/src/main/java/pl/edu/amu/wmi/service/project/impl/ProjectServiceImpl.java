package pl.edu.amu.wmi.service.project.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.*;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.mapper.project.ProjectMapper;
import pl.edu.amu.wmi.mapper.project.StudentProjectMapper;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;
import pl.edu.amu.wmi.model.project.StudentDTO;
import pl.edu.amu.wmi.service.grade.EvaluationCardService;
import pl.edu.amu.wmi.service.project.ProjectService;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.*;
import static pl.edu.amu.wmi.enumerations.UserRole.*;


@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDAO projectDAO;

    private final StudentDAO studentDAO;

    private final SupervisorDAO supervisorDAO;

    private final UserDataDAO userDataDAO;

    private final StudyYearDAO studyYearDAO;

    private final StudentProjectDAO studentProjectDAO;

    private final ExternalLinkDAO externalLinkDAO;

    private final ExternalLinkDefinitionDAO definitionDAO;

    private final RoleDAO roleDAO;

    private final ProjectMapper projectMapper;

    private final StudentProjectMapper studentMapper;

    private final EvaluationCardService evaluationCardService;

    @Autowired
    public ProjectServiceImpl(ProjectDAO projectDAO, StudentDAO studentDAO, SupervisorDAO supervisorDAO, UserDataDAO userDataDAO, StudyYearDAO studyYearDAO, StudentProjectDAO studentProjectDAO, RoleDAO roleDAO, ExternalLinkDAO externalLinkDAO, ExternalLinkDefinitionDAO definitionDAO, ProjectMapper projectMapper, StudentProjectMapper studentMapper, EvaluationCardService evaluationCardService) {
        this.projectDAO = projectDAO;
        this.studentDAO = studentDAO;
        this.supervisorDAO = supervisorDAO;
        this.userDataDAO = userDataDAO;
        this.studyYearDAO = studyYearDAO;
        this.studentProjectDAO = studentProjectDAO;
        this.roleDAO = roleDAO;
        this.externalLinkDAO = externalLinkDAO;
        this.definitionDAO = definitionDAO;
        this.projectMapper = projectMapper;
        this.studentMapper = studentMapper;
        this.evaluationCardService = evaluationCardService;
    }

    @Override
    public ProjectDetailsDTO findById(Long id) {
        Project project = projectDAO.findById(id).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", id)));
        List<StudentDTO> studentDTOs = new ArrayList<>();
        project.getAssignedStudents().stream()
                .forEach(studentProject -> {
                    StudentDTO studentDTO = studentMapper.mapToDto(studentProject.getStudent());
                    studentDTO.setRole(studentProject.getProjectRole());
                    studentDTO.setAccepted(studentProject.isProjectConfirmed());
                    studentDTOs.add(studentDTO);
                });
        ProjectDetailsDTO projectDetailsDTO = projectMapper.mapToDto(project);
        projectDetailsDTO.setStudents(studentDTOs);
        projectDetailsDTO.setAdmin(getIndexNumberOfProjectAdmin(project));
        return projectDetailsDTO;
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

            return prepareSortedProjectListWithRestrictions(projectEntityList, studentProjectsIds, byStudentAssignedAndConfirmedProjects);
        } else {
            List<Long> supervisorProjectIds = getSupervisorProjectIds(supervisor);
            Comparator<Project> bySupervisorAssignedAndAcceptedProjects = createComparatorBySupervisorAssignedAndAcceptedProjects(supervisor);

            if (isUserRoleCoordinator(userIndexNumber)) {
                projectEntityList.sort(bySupervisorAssignedAndAcceptedProjects);
                return projectMapper.mapToDTOs(projectEntityList);
            }
            return prepareSortedProjectListWithRestrictions(projectEntityList, supervisorProjectIds, bySupervisorAssignedAndAcceptedProjects);
        }
    }

    private Comparator<Project> createComparatorByStudentAssignedAndConfirmedProjects(List<Long> studentProjectsIds, Student student) {
        return Comparator
                .comparing((Project p) -> !studentProjectsIds.contains(p.getId()))
                .thenComparing((Project p) -> !isProjectEqualToStudentsConfirmed(p, student));
    }

    private List<Long> getStudentProjectsIds(Student student) {
        return student.getAssignedProjects().stream()
                .map(sp -> sp.getProject().getId()).toList();
    }

    private Comparator<Project> createComparatorBySupervisorAssignedAndAcceptedProjects(Supervisor supervisor) {
        return Comparator
                .comparing((Project p) -> !p.getSupervisor().equals(supervisor))
                .thenComparing((Project p) -> !p.getAcceptanceStatus().equals(ACCEPTED));
    }

    private List<Long> getSupervisorProjectIds(Supervisor supervisor) {
        return supervisor.getProjects().stream()
                .map(BaseAbstractEntity::getId)
                .toList();
    }

    private List<ProjectDTO> prepareSortedProjectListWithRestrictions(List<Project> projects, List<Long> userProjectIds,
                                                          Comparator<Project> comparator) {
        projects.sort(comparator);
        List<ProjectDTO> projectDTOs = new ArrayList<>();
        projects.forEach(project -> {
            if (userProjectIds.contains(project.getId())) {
                projectDTOs.add(projectMapper.mapToProjectDto(project));
            } else {
                projectDTOs.add(projectMapper.mapToProjectDtoWithRestrictions(project));
            }
        });
        return projectDTOs;
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
    public ProjectDetailsDTO saveProject(ProjectDetailsDTO project, String studyYear, String userIndexNumber) {
        Project projectEntity = projectMapper.mapToEntity(project);
        Supervisor supervisorEntity = supervisorDAO.findByStudyYearAndUserData_IndexNumber(studyYear, project.getSupervisor().getIndexNumber());
        StudyYear studyYearEntity = studyYearDAO.findByStudyYear(studyYear);
        Set<ExternalLinkDefinition> definitionEntities = definitionDAO.findAllByStudyYear_StudyYear(studyYear);
        Set<ExternalLink> externalLinkEntities = new HashSet<>();

        projectEntity.setSupervisor(supervisorEntity);
        projectEntity.setStudyYear(studyYearEntity);
        projectEntity.setAcceptanceStatus(acceptanceStatusByStudentsAmount(project));

        for (StudentDTO student : project.getStudents()) {
//            todo exception handling the student not found | add second param- study year to serach (after data-feed adjustments)
            Student entity = studentDAO.findByUserData_IndexNumber(student.getIndexNumber());
            if (isProjectAdmin(entity, userIndexNumber)) {
                entity.setProjectAdmin(true);
                entity.getUserData().getRoles().add(roleDAO.findByName(PROJECT_ADMIN));
            }

            projectEntity.addStudent(entity, student.getRole(), isProjectAdmin(entity, userIndexNumber));
        }

        // TODO: New method
        // External Links without URL creation
        definitionEntities.forEach(entity -> {
            ExternalLink externalLink = new ExternalLink();
            externalLink.setExternalLinkDefinition(entity);
            externalLink.setUrl(null);
            externalLinkDAO.save(externalLink);
            externalLinkEntities.add(externalLink);
        });

        projectEntity.setExternalLinks(externalLinkEntities);

        EvaluationCard evaluationCard = new EvaluationCard();
        projectEntity.setEvaluationCard(evaluationCard);
        evaluationCard.setProject(projectEntity);

        projectEntity = projectDAO.save(projectEntity);

        return projectMapper.mapToDto(projectEntity);
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

        Set<StudentProject> currentAssignedStudents = projectEntity.getAssignedStudents();
        Set<StudentProject> newAssignedStudents = getAssignedStudentsToUpdate(projectDetailsDTO);
        Set<StudentProject> assignedStudentsToRemove = getAssignedStudentsToRemove(currentAssignedStudents, newAssignedStudents);

        // TODO: 6/23/2023 Extract students removing to a new method
        if (isUserRoleCoordinator(userIndexNumber)) {
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

        if (isUserRoleCoordinator(userIndexNumber)) {
            projectEntity.getAssignedStudents().forEach(assignedStudent -> {
                assignedStudent.setProjectConfirmed(true);
                assignedStudent.getStudent().setProjectConfirmed(true);
                assignedStudent.getStudent().setConfirmedProject(projectEntity);
            });
        }

        updateProjectStatus(projectEntity);

        projectDAO.save(projectEntity);
        return projectMapper.mapToDto(projectEntity);
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

    private boolean isUserRoleCoordinator(String index) {
        UserData userData = userDataDAO.findByIndexNumber(index).orElseThrow(()
                -> new BusinessException(MessageFormat.format("User with index: {0} not found.", index)));
        return userData.getRoles().stream().anyMatch(role -> role.getName().equals(COORDINATOR));
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

        ProjectDetailsDTO projectDetailsDTO = projectMapper.mapToDto(projectEntity);
        projectDetailsDTO.setAdmin(newAdminStudentEntity.getIndexNumber());

        return projectDetailsDTO;

    }

    @Override
    @Transactional
    public ProjectDetailsDTO acceptProject(String studyYear, String userIndexNumber, Long projectId) {

        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        if (getUserRoleByUserIndex(userIndexNumber).equals(STUDENT)) {
            StudentProject studentProjectEntity = getStudentProjectByStudentIndex(projectEntity, userIndexNumber);
            studentProjectEntity.setProjectConfirmed(true);
            studentProjectEntity.getStudent().setProjectConfirmed(true);
            studentProjectEntity.getStudent().setConfirmedProject(projectEntity);

            if (isProjectConfirmedByAllStudents(projectEntity)) {
                projectEntity.setAcceptanceStatus(CONFIRMED);
            }

            studentProjectDAO.save(studentProjectEntity);

        } else if (getUserRoleByUserIndex(userIndexNumber).equals(SUPERVISOR)) {
            log.info("Project with id: {} was accepted by all students and a supervisor", projectId);
            evaluationCardService.addEmptyGradesToEvaluationCard(projectEntity, studyYear);
            projectEntity.setAcceptanceStatus(ACCEPTED);
        } else {
            // TODO: 6/3/2023 handle wrong role exception
            log.error("Wrong user role - role must be a {} or {}.", STUDENT, SUPERVISOR);
        }

        projectDAO.save(projectEntity);

        return projectMapper.mapToDto(projectEntity);

    }

    @Override
    public ProjectDetailsDTO unAcceptProject(String studyYear, String userIndexNumber, Long projectId) {
        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        if (getUserRoleByUserIndex(userIndexNumber).equals(STUDENT)) {
            if (isProjectConfirmedByAllStudents(projectEntity)) {
                projectEntity.setAcceptanceStatus(PENDING);
            }
            StudentProject studentProjectEntity = getStudentProjectByStudentIndex(projectEntity, userIndexNumber);
            studentProjectEntity.setProjectConfirmed(false);
            studentProjectEntity.getStudent().setProjectConfirmed(false);
            studentProjectEntity.getStudent().setConfirmedProject(null);

            studentProjectDAO.save(studentProjectEntity);

        }
        return projectMapper.mapToDto(projectEntity);
    }

    @Transactional
    @Override
    public void delete(Long projectId, String userIndexNumber) throws ProjectManagementException {
        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        if (!validateDeletionPermission(userIndexNumber, projectEntity)) {
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

    private boolean validateDeletionPermission(String userIndexNumber, Project project) {
        UserData userDataEntity = userDataDAO.findByIndexNumber(userIndexNumber).orElseThrow(()
                -> new BusinessException(MessageFormat.format("User with index: {0} not found", userIndexNumber)));
        List<UserRole> userRoles = userDataEntity.getRoles().stream()
                .map(Role::getName)
                .toList();
        if (userRoles.contains(COORDINATOR)) {
            return true;
        } else if (userRoles.contains(PROJECT_ADMIN)) {
            if (ACCEPTED == project.getAcceptanceStatus()) {
                return false;
            } else return isStudentAnAdminOfTheProject(userIndexNumber, project.getId());
        }
        return false;
    }

    private boolean isStudentAnAdminOfTheProject(String userIndexNumber, Long projectId) {
        Student student = studentDAO.findByUserData_IndexNumber(userIndexNumber);
        return Objects.equals(student.getConfirmedProject().getId(), projectId) &&
                student.isProjectAdmin();
    }


    private boolean isProjectConfirmedByAllStudents(Project project) {
        return project.getAssignedStudents().stream().allMatch(StudentProject::isProjectConfirmed);
    }

    private UserRole getUserRoleByUserIndex(String index) {
        UserData userData = userDataDAO.findByIndexNumber(index).orElseThrow(()
                -> new BusinessException(MessageFormat.format("User with index: {0} not found.", index)));
        Role userRole = userData.getRoles().stream()
                .filter(role -> role.getName().equals(STUDENT) || role.getName().equals(SUPERVISOR))
                .findFirst().orElseThrow(()
                        -> new BusinessException(MessageFormat.format("User with index: {0} does not have required role.", index)));
        return userRole.getName();
    }

    private boolean isProjectAdmin(Student entity, String userIndexNumber) {
        return Objects.equals(userIndexNumber, entity.getIndexNumber());
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
            StudentProject studentProject = studentProjectDAO.findByStudent_IdAndProject_Id(student.getId(), projectDetailsDTO.getId());
            studentProjectsForUpdate.add(studentProject);
        });
        return studentProjectsForUpdate;
    }

    private Set<StudentProject> getAssignedStudentsToRemove(Set<StudentProject> currentAssignedStudents, Set<StudentProject> newAssignedStudents) {
        return currentAssignedStudents.stream()
                .filter(element -> !newAssignedStudents.contains(element))
                .collect(Collectors.toSet());
    }

}
