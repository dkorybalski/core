package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.ExternalLinkDTO;
import pl.edu.amu.wmi.model.ExternalLinkDataDTO;

import java.util.List;
import java.util.Set;

public interface ExternalLinkService {

    List<ExternalLinkDataDTO> findAll();

    Set<ExternalLinkDTO> findByProjectId(Long id);

    Set<ExternalLinkDTO> updateExternalLinks(Long projectId, Set<ExternalLinkDTO> externalLinks);

}
