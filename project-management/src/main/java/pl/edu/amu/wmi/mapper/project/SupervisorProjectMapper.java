package pl.edu.amu.wmi.mapper.project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.model.project.SupervisorAvailabilityDTO;
import pl.edu.amu.wmi.model.project.SupervisorDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupervisorProjectMapper {

    @Mapping(target = "name", expression = "java(String.format(\"%s %s\", entity.getUserData().getFirstName(), entity.getUserData().getLastName()))")
    @Mapping(target = "email", source = "userData.email")
    @Mapping(target = "indexNumber", source = "userData.indexNumber")
    SupervisorDTO mapToDto(Supervisor entity);

    @Mapping(target = "supervisor", source = "entity")
    @Mapping(target = "max", source = "maxNumberOfProjects")
    SupervisorAvailabilityDTO mapToAvailabilityDto(Supervisor entity);

    List<SupervisorAvailabilityDTO> mapToAvailabilityDtoList(List<Supervisor> entityList);

}
