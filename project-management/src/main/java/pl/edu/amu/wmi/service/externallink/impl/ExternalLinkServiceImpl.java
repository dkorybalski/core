package pl.edu.amu.wmi.service.externallink.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.ExternalLinkDAO;
import pl.edu.amu.wmi.dao.ExternalLinkDefinitionDAO;
import pl.edu.amu.wmi.entity.ExternalLink;
import pl.edu.amu.wmi.entity.ExternalLinkDefinition;
import pl.edu.amu.wmi.exception.externallink.ExternalLinkException;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;
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


    @Autowired
    public ExternalLinkServiceImpl(ExternalLinkDAO externalLinkDAO, ExternalLinkDefinitionDAO externalLinkDefinitionDAO) {
        this.externalLinkDAO = externalLinkDAO;
        this.externalLinkDefinitionDAO = externalLinkDefinitionDAO;
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
    public List<ExternalLink> updateExternalLinks(Set<ExternalLinkDTO> externalLinks) {
        Set<ExternalLink> externalLinkEntities = new HashSet<>();

        externalLinks.forEach(externalLinkDto -> {
            ExternalLink externalLink = externalLinkDAO.findById(
                    Long.valueOf(externalLinkDto.getId())).orElseThrow(()
                    -> new ExternalLinkException(MessageFormat.format("External link with id: {0} not found.", externalLinkDto.getId())));
            externalLink.setUrl(externalLinkDto.getUrl());
            externalLinkEntities.add(externalLink);
        });

        return externalLinkDAO.saveAll(externalLinkEntities);
    }

    @Override
    public Set<String> findDefinitionHeadersByStudyYear(String studyYear) {
        return externalLinkDefinitionDAO.findAllByStudyYear_StudyYear(studyYear)
                .stream()
                .map(ExternalLinkDefinition::getColumnHeader)
                .collect(Collectors.toSet());
    }

}
