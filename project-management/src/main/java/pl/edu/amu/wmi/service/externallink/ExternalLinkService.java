package pl.edu.amu.wmi.service.externallink;

import pl.edu.amu.wmi.entity.ExternalLink;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;

import java.util.List;
import java.util.Set;

public interface ExternalLinkService {

    Set<ExternalLink> createEmptyExternalLinks(String studyYear);

    List<ExternalLink> updateExternalLinks(Set<ExternalLinkDTO> externalLinks);

    Set<String> findDefinitionHeadersByStudyYear(String studyYear);

}
