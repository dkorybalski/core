package pl.edu.amu.wmi.service.externallink.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ExternalLinkDAO;
import pl.edu.amu.wmi.dao.ExternalLinkDefinitionDAO;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.BaseAbstractEntity;
import pl.edu.amu.wmi.entity.ExternalLink;
import pl.edu.amu.wmi.entity.ExternalLinkDefinition;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.exception.externallink.ExternalLinkException;
import pl.edu.amu.wmi.exception.project.ProjectManagementException;
import pl.edu.amu.wmi.mapper.externallink.ExternalLinkMapper;
import pl.edu.amu.wmi.mapper.project.SupervisorProjectMapper;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDataDTO;
import pl.edu.amu.wmi.service.externallink.ExternalLinkService;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                .toList();

    }

    @Override
    public Set<ExternalLinkDTO> findByProjectId(Long projectId) {

        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        Set<Long> projectLinksIds = projectEntity.getExternalLinks().stream()
                .map(BaseAbstractEntity::getId)
                .collect(Collectors.toSet());

        return externalLinkDAO.findAllById(projectLinksIds).stream()
                .map(externalLinkMapper::mapToDto)
                .collect(Collectors.toSet());
    }

    @Transactional
    @Override
    public Set<ExternalLink> createEmptyExternalLinks(String studyYear) {
        Set<ExternalLinkDefinition> definitionEntities = externalLinkDefinitionDAO.findAllByStudyYear_StudyYear(studyYear);
        Set<ExternalLink> externalLinkEntities = new HashSet<>();

        definitionEntities.forEach(entity ->
                externalLinkEntities.add(createEmptyExternalLink(entity))
        );
        return externalLinkEntities;
    }

    private ExternalLink createEmptyExternalLink(ExternalLinkDefinition definition) {
        ExternalLink externalLink = new ExternalLink();
        externalLink.setExternalLinkDefinition(definition);
        externalLink.setUrl(null);
        return externalLinkDAO.save(externalLink);
    }

    @Transactional
    @Override
    public Set<ExternalLinkDTO> updateExternalLinks(Long projectId, Set<ExternalLinkDTO> externalLinks) {

        Project projectEntity = projectDAO.findById(projectId).orElseThrow(()
                -> new ProjectManagementException(MessageFormat.format("Project with id: {0} not found", projectId)));

        Set<ExternalLink> externalLinkEntities = new HashSet<>();

        externalLinks.forEach(externalLinkDto -> {
            ExternalLink externalLink = externalLinkDAO.findById(
                    externalLinkDto.getId()).orElseThrow(()
                    -> new ExternalLinkException(MessageFormat.format("External link with id: {0} not found.", externalLinkDto.getId())));
            externalLink.setUrl(externalLinkDto.getUrl());
            externalLinkEntities.add(externalLink);
        });

        externalLinkDAO.saveAll(externalLinkEntities);

        projectEntity.setExternalLinks(externalLinkEntities);

        projectDAO.save(projectEntity);

        return externalLinkMapper.mapToDtoSet(projectEntity.getExternalLinks());
    }

    @Override
    public Set<String> findDefinitionHeadersByStudyYear(String studyYear) {
        return externalLinkDefinitionDAO.findAllByStudyYear_StudyYear(studyYear)
                .stream()
                .map(ExternalLinkDefinition::getColumnHeader)
                .collect(Collectors.toSet());
    }

}
