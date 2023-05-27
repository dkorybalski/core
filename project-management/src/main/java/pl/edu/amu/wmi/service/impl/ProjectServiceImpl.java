package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.dao.StudyYearDAO;
import pl.edu.amu.wmi.dao.SupervisorDAO;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.StudyYear;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.mapper.ProjectMapper;
import pl.edu.amu.wmi.model.ProjectCreationRequestDTO;
import pl.edu.amu.wmi.model.ProjectCreationResponseDTO;
import pl.edu.amu.wmi.service.ProjectService;

import java.util.List;

@Slf4j
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDAO projectDAO;

    private final SupervisorDAO supervisorDAO;

    private final StudyYearDAO studyYearDAO;

    private final ProjectMapper projectMapper;

    @Autowired
    public ProjectServiceImpl(ProjectDAO projectDAO, SupervisorDAO supervisorDAO, StudyYearDAO studyYearDAO, ProjectMapper projectMapper) {
        this.projectDAO = projectDAO;
        this.supervisorDAO = supervisorDAO;
        this.studyYearDAO = studyYearDAO;
        this.projectMapper = projectMapper;
    }

    @Override
    public List<ProjectCreationRequestDTO> findAll() {
        return projectMapper.mapToDtoList(projectDAO.findAll());
    }

    @Override
    @Transactional
    public ProjectCreationResponseDTO saveProject(ProjectCreationRequestDTO project) {
        Project projectEntity = projectMapper.mapToEntity(project);
        Supervisor supervisorEntity = supervisorDAO.findById(project.getSupervisorId()).get();
        StudyYear studyYearEntity = studyYearDAO.findByStudyYear(project.getStudyYear());
        projectEntity.setSupervisor(supervisorEntity);
        projectEntity.setStudyYear(studyYearEntity);
        return projectMapper.mapToResponseDto(projectDAO.save(projectEntity));
    }
}