package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.model.user.SupervisorCreationRequestDTO;
import pl.edu.amu.wmi.model.user.SupervisorDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupervisorUserMapper {

    @Mapping(target = "name", source = "entity", qualifiedByName = "SupervisorToSupervisorFullName")
    @Mapping(target = "email", source = "userData.email")
    @Mapping(target = "indexNumber", source = "userData.indexNumber")
    @Mapping(target = "initials", source = "entity", qualifiedByName = "SupervisorToSupervisorInitials")
    SupervisorDTO mapToDto(Supervisor entity);

    List<SupervisorDTO> mapToDtoList(List<Supervisor> entityList);

    @Mapping(target = "userData.firstName", source = "dto.name")
    @Mapping(target = "userData.lastName", source = "dto.surname")
    @Mapping(target = "userData.email", source = "dto.email")
    @Mapping(target = "userData.indexNumber", source = "dto.indexNumber")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "maxNumberOfProjects", ignore = true)
    Supervisor createEntity(SupervisorCreationRequestDTO dto);

    @Named("SupervisorToSupervisorInitials")
    default String supervisorToSupervisorInitials(Supervisor supervisor) {
        return supervisor.getInitials();
    }

    @Named("SupervisorToSupervisorFullName")
    default String supervisorToSupervisorFullName(Supervisor supervisor) {
        return supervisor.getFullName();
    }

}