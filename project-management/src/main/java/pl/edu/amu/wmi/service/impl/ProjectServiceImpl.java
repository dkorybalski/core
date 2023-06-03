package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.*;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.mapper.ProjectMapper;
import pl.edu.amu.wmi.model.ProjectDTO;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;
import pl.edu.amu.wmi.model.StudentDTO;
import pl.edu.amu.wmi.service.ProjectService;

import java.util.*;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.*;
import static pl.edu.amu.wmi.enumerations.UserRole.STUDENT;
import static pl.edu.amu.wmi.enumerations.UserRole.SUPERVISOR;


@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDAO projectDAO;

    private final StudentDAO studentDAO;

    private final SupervisorDAO supervisorDAO;

    private final UserDataDAO userDataDAO;

    private final StudyYearDAO studyYearDAO;

    private final StudentProjectDAO studentProjectDAO;

    private final ProjectMapper projectMapper;

    @Autowired
    public ProjectServiceImpl(ProjectDAO projectDAO, StudentDAO studentDAO, SupervisorDAO supervisorDAO, UserDataDAO userDataDAO, StudyYearDAO studyYearDAO, StudentProjectDAO studentProjectDAO, ProjectMapper projectMapper) {
        this.projectDAO = projectDAO;
        this.studentDAO = studentDAO;
        this.supervisorDAO = supervisorDAO;
        this.userDataDAO = userDataDAO;
        this.studyYearDAO = studyYearDAO;
        this.studentProjectDAO = studentProjectDAO;
        this.projectMapper = projectMapper;
    }

    // TODO: 6/3/2023 handle optional .get()
    @Override
    public ProjectDetailsDTO findById(Long id) {
        return projectMapper.mapToDto(projectDAO.findById(id).get());
    }

    // TODO: 5/31/2023 Reimplement this method using Criteria Queries; use user roles
    @Override
    public List<ProjectDTO> findAll(String studyYear, String userIndexNumber) {
        List<Project> projectEntityList = projectDAO.findAllByStudyYear_StudyYear(studyYear);
        Student studentByIndexNumber = studentDAO.findByUserData_IndexNumber(userIndexNumber);

        projectEntityList.sort(Comparator
                .comparing((Project p) -> p.getAssignedStudents().stream().noneMatch(studentProject -> studentProject.getStudent().equals(studentByIndexNumber)))
                .thenComparing(p -> !p.getSupervisor().getUserData().getIndexNumber().equals(userIndexNumber))
                .thenComparing(Project::getId, Comparator.naturalOrder()));

        return projectMapper.mapToDtoList(projectEntityList);
    }


    @Override
    @Transactional
    public ProjectDetailsDTO saveProject(ProjectDetailsDTO project, String studyYear, String userIndexNumber) {
        Project projectEntity = projectMapper.mapToEntity(project);
        // TODO: 5/28/2023 change to find by indexNumber after adjustments in data feed
        Supervisor supervisorEntity = supervisorDAO.findById(project.getSupervisor().getId()).get();
        StudyYear studyYearEntity = studyYearDAO.findByStudyYear(studyYear);

        projectEntity.setSupervisor(supervisorEntity);
        projectEntity.setStudyYear(studyYearEntity);
        projectEntity.setAcceptanceStatus(PENDING);

        for (StudentDTO student : project.getStudents()) {
//            todo exception handling the student not found | add second param- study year to serach (after data-feed adjustments)
            Student entity = studentDAO.findByUserData_IndexNumber(student.getIndexNumber());
            projectEntity.addStudent(entity, student.getRole(), isProjectAdmin(entity, userIndexNumber));
        }

        projectEntity = projectDAO.save(projectEntity);

        return projectMapper.mapToDto(projectEntity);
    }

    @Override
    @Transactional
    public ProjectDetailsDTO updateProjectAdmin(Long projectId, String studentIndex) {

        Project projectEntity = projectDAO.findById(projectId).get();

        StudentProject currentAdminStudentProjectEntity = getStudentProjectOfAdmin(projectEntity);
        currentAdminStudentProjectEntity.setProjectAdmin(false);
        studentProjectDAO.save(currentAdminStudentProjectEntity);

        StudentProject newAdminStudentProjectEntity = getStudentProjectByStudentIndex(projectEntity, studentIndex);
        newAdminStudentProjectEntity.setProjectAdmin(true);
        studentProjectDAO.save(newAdminStudentProjectEntity);

        Student currentAdminStudentEntity = currentAdminStudentProjectEntity.getStudent();
        Student newAdminStudentEntity = newAdminStudentProjectEntity.getStudent();

        if (isProjectConfirmedOrAccepted(projectEntity)) {
            currentAdminStudentEntity.setProjectAdmin(false);
            newAdminStudentEntity.setProjectAdmin(true);
            studentDAO.save(currentAdminStudentEntity);
            studentDAO.save(newAdminStudentEntity);
        }

        ProjectDetailsDTO projectDetailsDTO = projectMapper.mapToDto(projectEntity);
        projectDetailsDTO.setAdmin(newAdminStudentEntity.getUserData().getIndexNumber());

        return projectDetailsDTO;

    }

    @Override
    @Transactional
    public ProjectDetailsDTO acceptProject(String studyYear, String userIndexNumber, Long projectId) {

        Project projectEntity = projectDAO.findById(projectId).get();

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
            projectEntity.setAcceptanceStatus(ACCEPTED);
        } else {
            // TODO: 6/3/2023 handle wrong role exception
            log.error("Wrong user role - role must be a {} or {}.", STUDENT, SUPERVISOR);
        }

        projectDAO.save(projectEntity);

        return projectMapper.mapToDto(projectEntity);

    }

    private boolean isProjectConfirmedByAllStudents(Project project) {
        return project.getAssignedStudents().stream().allMatch(StudentProject::isProjectConfirmed);
    }

    private UserRole getUserRoleByUserIndex(String index) {
        return userDataDAO.findByIndexNumber(index).getRoles().stream()
                .filter(role -> role.getName().equals(STUDENT) || role.getName().equals(SUPERVISOR))
                .findFirst().get().getName();
    }

    private boolean isProjectAdmin(Student entity, String userIndexNumber) {
        return Objects.equals(userIndexNumber, entity.getUserData().getIndexNumber());
    }

    private boolean isProjectConfirmedOrAccepted(Project project) {
        return project.getAcceptanceStatus().equals(CONFIRMED) || project.getAcceptanceStatus().equals(ACCEPTED);
    }

    // TODO: 6/3/2023 handle optional .get()
    private StudentProject getStudentProjectOfAdmin(Project project) {
        return project.getAssignedStudents().stream()
                .filter(StudentProject::isProjectAdmin)
                .findFirst().get();
    }

    // TODO: 6/3/2023 handle optional .get()
    private StudentProject getStudentProjectByStudentIndex(Project project, String index) {
        return project.getAssignedStudents().stream()
                .filter(studentProject -> studentProject.getStudent().getUserData().getIndexNumber().equals(index))
                .findFirst().get();
    }
}