package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.model.user.SupervisorCreationRequestDTO;
import pl.edu.amu.wmi.model.user.SupervisorDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupervisorUserMapper {

    @Mapping(target = "name", expression = "java(String.format(\"%s %s\", entity.getUserData().getFirstName(), entity.getUserData().getLastName()))")
    @Mapping(target = "email", source = "userData.email")
    @Mapping(target = "indexNumber", source = "userData.indexNumber")
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
}