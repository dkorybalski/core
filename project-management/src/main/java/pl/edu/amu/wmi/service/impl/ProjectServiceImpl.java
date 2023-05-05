package pl.edu.amu.wmi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.mapper.ProjectMapper;
import pl.edu.amu.wmi.model.ProjectDTO;
import pl.edu.amu.wmi.service.ProjectService;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDAO projectDAO;

    private final ProjectMapper projectMapper;

    @Autowired
    public ProjectServiceImpl(ProjectDAO projectDAO, ProjectMapper projectMapper) {
        this.projectDAO = projectDAO;
        this.projectMapper = projectMapper;
    }

    @Override
    public List<ProjectDTO> findAll() {
        return projectMapper.map(projectDAO.findAll());
    }

    @Override
    @Transactional
    public ProjectDTO saveProject(ProjectDTO project) {
        return projectMapper.map(projectDAO.save(projectMapper.map(project)));
    }
}
