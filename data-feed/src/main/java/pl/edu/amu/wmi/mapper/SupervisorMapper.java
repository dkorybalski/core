package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.model.NewSupervisorDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupervisorMapper {

    @Mapping(target = "userData.firstName", source = "dto.firstName")
    @Mapping(target = "userData.lastName", source = "dto.lastName")
    @Mapping(target = "userData.email", source = "dto.email")
    @Mapping(target = "userData.indexNumber", source = "dto.indexNumber")
    Supervisor mapToEntity(NewSupervisorDTO dto);

    List<Supervisor> mapToEntities(List<NewSupervisorDTO> dtos);

    NewSupervisorDTO mapToDTO(Supervisor entity);

    List<NewSupervisorDTO> mapToDTOs(List<Supervisor> entities);
}
