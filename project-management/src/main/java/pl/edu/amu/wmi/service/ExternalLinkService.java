package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.model.ExternalLinkDataDTO;

import java.util.List;

public interface ExternalLinkService {

    List<ExternalLinkDataDTO> findAll();

    ExternalLinkDataDTO findByProjectId(Long id);

    ExternalLinkDataDTO updateExternalLinkData(ExternalLinkDataDTO externalLinkData);

    ExternalLinkDataDTO saveExternalLinkData(ExternalLinkDataDTO externalLinkData);

}
