package pl.edu.amu.wmi.mapper.externallink;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.ExternalLink;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;

import java.util.Set;


@Mapper(componentModel = "spring")
public interface ExternalLinkMapper {

    ExternalLink mapToEntity(ExternalLinkDTO dto);

    @Mapping(target = "name", source = "externalLinkDefinition.name")
    @Mapping(target = "columnHeader", source = "externalLinkDefinition.columnHeader")
    @Mapping(target = "deadline", source = "externalLinkDefinition.deadline")
    ExternalLinkDTO mapToDto(ExternalLink entity);

    Set<ExternalLinkDTO> mapToDtoSet(Set<ExternalLink> externalLinks);

}
