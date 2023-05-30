package pl.edu.amu.wmi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ExternalLinkDAO;
import pl.edu.amu.wmi.dao.ExternalLinkDefinitionDAO;
import pl.edu.amu.wmi.dao.ProjectDAO;
import pl.edu.amu.wmi.entity.BaseAbstractEntity;
import pl.edu.amu.wmi.entity.ExternalLink;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.mapper.ExternalLinkMapper;
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

    @Autowired
    public ExternalLinkServiceImpl(ExternalLinkDAO externalLinkDAO, ExternalLinkDefinitionDAO externalLinkDefinitionDAO, ProjectDAO projectDAO, ExternalLinkMapper externalLinkMapper) {
        this.externalLinkDAO = externalLinkDAO;
        this.externalLinkDefinitionDAO = externalLinkDefinitionDAO;
        this.projectDAO = projectDAO;
        this.externalLinkMapper = externalLinkMapper;
    }

    @Override
    public List<ExternalLinkDataDTO> findAll() {
        return null;
    }

    @Override
    public ExternalLinkDataDTO findByProjectId(Long projectId) {

        Project projectEntity = projectDAO.findById(projectId).get();
        String projectName = projectEntity.getName();
        Supervisor supervisor = projectEntity.getSupervisor();

        Set<Long> projectLinksIds = projectEntity.getExternalLinks().stream()
                .map(BaseAbstractEntity::getId)
                .collect(Collectors.toSet());

        Set<ExternalLinkDTO> externalLinkDTOs = externalLinkDAO.findAllById(projectLinksIds).stream()
                .map(externalLinkMapper::mapToDto)
                .collect(Collectors.toSet());

        return new ExternalLinkDataDTO(
                projectId,
                projectName,
                externalLinkMapper.mapToDto(supervisor),
                externalLinkDTOs
        );

    }

    @Override
    public ExternalLinkDataDTO updateExternalLinkData(ExternalLinkDataDTO externalLinkData) {

        Long projectId = externalLinkData.getProjectId();
        Project projectEntity = projectDAO.findById(projectId).get();
        String projectName = projectEntity.getName();
        Supervisor supervisor = projectEntity.getSupervisor();

        Set<ExternalLink> externalLinkEntities = new HashSet<>();

        externalLinkData.getExternalLinks().forEach(externalLinkDTO -> {
            ExternalLink externalLinkEntity = externalLinkDAO.findById(externalLinkDTO.getId()).get();
            externalLinkEntity.setUrl(externalLinkDTO.getUrl());
            externalLinkEntities.add(externalLinkEntity);
            // TODO: Remove save method when Cascades are configured.
            externalLinkDAO.save(externalLinkEntity);
        });

        projectEntity.setExternalLinks(externalLinkEntities);
        projectDAO.save(projectEntity);

        return new ExternalLinkDataDTO(
                projectId,
                projectName,
                externalLinkMapper.mapToDto(supervisor),
                externalLinkMapper.mapToDtoSet(projectEntity.getExternalLinks())
        );

    }

    @Override
    @Transactional
    public ExternalLinkDataDTO saveExternalLinkData(ExternalLinkDataDTO externalLinkData) {

        Long projectId = externalLinkData.getProjectId();
        Project projectEntity = projectDAO.findById(projectId).get();
        String projectName = projectEntity.getName();
        Supervisor supervisor = projectEntity.getSupervisor();


        externalLinkData.getExternalLinks().forEach(externalLinkDTO -> {
            ExternalLink externalLinkEntity = externalLinkMapper.mapToEntity(externalLinkDTO);
            externalLinkEntity.setExternalLinkDefinition(externalLinkDefinitionDAO.findById(externalLinkDTO.getExternalLinkDefinition().getId()).get());
            // TODO: Remove save method when Cascades are configured.
            externalLinkDAO.save(externalLinkEntity);
            projectEntity.addExternalLink(externalLinkEntity);
        });

        projectDAO.save(projectEntity);

        return new ExternalLinkDataDTO(
                projectId,
                projectName,
                externalLinkMapper.mapToDto(supervisor),
                externalLinkMapper.mapToDtoSet(projectEntity.getExternalLinks())
        );

    }

}
