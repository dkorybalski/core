package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.model.ProjectDTO;
import pl.edu.amu.wmi.model.ProjectDetailsDTO;

import java.util.List;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.ACCEPTED;
import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.CONFIRMED;

@Mapper(componentModel = "spring", uses = { SupervisorProjectMapper.class, ExternalLinkMapper.class })
public interface ProjectMapper {

    @Mapping(target = "supervisor", ignore = true)
    Project mapToEntity(ProjectDetailsDTO dto);

    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "confirmed", source = "acceptanceStatus", qualifiedByName = "ConfirmedToBoolean")
    ProjectDetailsDTO mapToDto(Project project);


    @Mapping(target = "supervisor", source = "entity.supervisor")
    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    ProjectDTO mapToProjectDto(Project entity);

    List<ProjectDTO> mapToDtoList(List<Project> entityList);

    @Named("AcceptedToBoolean")
    default boolean mapAccepted(AcceptanceStatus status) {
        return status.equals(ACCEPTED);
    }

    @Named("ConfirmedToBoolean")
    default boolean mapConfirmed(AcceptanceStatus status) {
        return status.equals(CONFIRMED) || status.equals(ACCEPTED);
    }

}
