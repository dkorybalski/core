package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import pl.edu.amu.wmi.entity.ExternalLink;
import pl.edu.amu.wmi.model.ExternalLinkDTO;

import java.util.Set;


@Mapper(componentModel = "spring")
public interface ExternalLinkMapper {

    ExternalLink mapToEntity(ExternalLinkDTO dto);

    ExternalLinkDTO mapToDto(ExternalLink entity);

    Set<ExternalLinkDTO> mapToDtoSet(Set<ExternalLink> externalLinks);

}
