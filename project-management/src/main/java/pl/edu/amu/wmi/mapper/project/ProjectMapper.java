package pl.edu.amu.wmi.mapper.project;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.mapper.externallink.ExternalLinkMapper;
import pl.edu.amu.wmi.mapper.grade.PointsMapper;
import pl.edu.amu.wmi.model.project.ProjectDTO;
import pl.edu.amu.wmi.model.project.ProjectDetailsDTO;

import java.util.List;

import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.ACCEPTED;
import static pl.edu.amu.wmi.enumerations.AcceptanceStatus.CONFIRMED;

@Mapper(componentModel = "spring", uses = { SupervisorProjectMapper.class, ExternalLinkMapper.class, PointsMapper.class })
public interface ProjectMapper {

    @Mapping(target = "supervisor", ignore = true)
    Project mapToEntity(ProjectDetailsDTO dto);

    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "confirmed", source = "acceptanceStatus", qualifiedByName = "ConfirmedToBoolean")
    ProjectDetailsDTO mapToProjectDetailsDto(Project project);

    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "confirmed", source = "acceptanceStatus", qualifiedByName = "ConfirmedToBoolean")
    @Mapping(target = "externalLinks", ignore = true)
    ProjectDetailsDTO mapToProjectDetailsWithRestrictionsDto(Project project);


    @Mapping(target = "supervisor", source = "entity.supervisor")
    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "pointsFirstSemester", ignore = true)
    @Mapping(target = "pointsSecondSemester", ignore = true)
    @Mapping(target = "criteriaMet", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Named("mapWithoutRestrictions")
    ProjectDTO mapToProjectDto(Project entity);

    @Named("AcceptedToBoolean")
    default boolean mapAccepted(AcceptanceStatus status) {
        return status.equals(ACCEPTED);
    }

    @Named("ConfirmedToBoolean")
    default boolean mapConfirmed(AcceptanceStatus status) {
        return status.equals(CONFIRMED) || status.equals(ACCEPTED);
    }

    @Named("DisqualifiedToCriteriaMet")
    default boolean mapCriteriaMet(boolean isDisqualified) {
        return !isDisqualified;
    }

    @Mapping(target = "supervisor", source = "entity.supervisor")
    @Mapping(target = "accepted", source = "acceptanceStatus", qualifiedByName = "AcceptedToBoolean")
    @Mapping(target = "pointsFirstSemester", ignore = true)
    @Mapping(target = "pointsSecondSemester", ignore = true)
    @Mapping(target = "criteriaMet", ignore = true)
    @Mapping(target = "externalLinks", ignore = true)
    @Mapping(target = "students", ignore = true)
    ProjectDTO mapToProjectDtoWithRestrictions(Project entity);

    @IterableMapping(qualifiedByName = "mapWithoutRestrictions")
    List<ProjectDTO> mapToDTOs(List<Project> projectEntityList);

}
