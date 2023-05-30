package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import pl.edu.amu.wmi.entity.ExternalLink;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.model.ExternalLinkDTO;
import pl.edu.amu.wmi.model.SupervisorDTO;

import java.util.Set;


@Mapper(componentModel = "spring")
public interface ExternalLinkMapper {

    ExternalLink mapToEntity(ExternalLinkDTO dto);

    ExternalLinkDTO mapToDto(ExternalLink entity);

    SupervisorDTO mapToDto(Supervisor supervisor);

    Set<ExternalLinkDTO> mapToDtoSet(Set<ExternalLink> externalLinks);

}
