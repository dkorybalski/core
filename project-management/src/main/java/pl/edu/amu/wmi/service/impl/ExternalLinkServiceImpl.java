package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ExternalLinkDAO;
import pl.edu.amu.wmi.dao.ExternalLinkDefinitionDAO;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.*;
import pl.edu.amu.wmi.mapper.ExternalLinkMapper;
import pl.edu.amu.wmi.mapper.SupervisorProjectMapper;
import pl.edu.amu.wmi.model.ExternalLinkDTO;
import pl.edu.amu.wmi.model.ExternalLinkDataDTO;
import pl.edu.amu.wmi.service.ExternalLinkService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExternalLinkServiceImpl implements ExternalLinkService {

    private final ExternalLinkDAO externalLinkDAO;

    private final ExternalLinkDefinitionDAO externalLinkDefinitionDAO;

    private final ProjectDAO projectDAO;

    private final ExternalLinkMapper externalLinkMapper;

    private final SupervisorProjectMapper supervisorMapper;

    @Autowired
    public ExternalLinkServiceImpl(ExternalLinkDAO externalLinkDAO, ExternalLinkDefinitionDAO externalLinkDefinitionDAO, ProjectDAO projectDAO, ExternalLinkMapper externalLinkMapper, SupervisorProjectMapper supervisorMapper) {
        this.externalLinkDAO = externalLinkDAO;
        this.externalLinkDefinitionDAO = externalLinkDefinitionDAO;
        this.projectDAO = projectDAO;
        this.externalLinkMapper = externalLinkMapper;
        this.supervisorMapper = supervisorMapper;
    }

    @Override
    public List<ExternalLinkDataDTO> findAll() {

        List<Project> projectEntityList = projectDAO.findAll();

        return projectEntityList.stream()
                .map(project -> new ExternalLinkDataDTO(
                        project.getId(),
                        project.getName(),
                        supervisorMapper.mapToDto(project.getSupervisor()),
                        externalLinkMapper.mapToDtoSet(project.getExternalLinks())))
                .collect(Collectors.toList());

    }

    @Override
    public Set<ExternalLinkDTO> findByProjectId(Long projectId) {

        Project projectEntity = projectDAO.findById(projectId).get();

        Set<Long> projectLinksIds = projectEntity.getExternalLinks().stream()
                .map(BaseAbstractEntity::getId)
                .collect(Collectors.toSet());

        return externalLinkDAO.findAllById(projectLinksIds).stream()
                .map(externalLinkMapper::mapToDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    @Override
    public Set<ExternalLinkDTO> updateExternalLinks(Long projectId, Set<ExternalLinkDTO> externalLinks) {

        Project projectEntity = projectDAO.findById(projectId).get();

        Set<ExternalLink> externalLinkEntities = new HashSet<>();

        // TODO: Handle optional
        externalLinks.forEach(externalLinkDto -> {
            ExternalLink externalLink = externalLinkDAO.findById(externalLinkDto.getId()).get();
            externalLink.setUrl(externalLinkDto.getUrl());
            externalLinkEntities.add(externalLink);
        });

        externalLinkDAO.saveAll(externalLinkEntities);

        projectEntity.setExternalLinks(externalLinkEntities);

        projectDAO.save(projectEntity);

        return externalLinkMapper.mapToDtoSet(projectEntity.getExternalLinks());
    }

}
