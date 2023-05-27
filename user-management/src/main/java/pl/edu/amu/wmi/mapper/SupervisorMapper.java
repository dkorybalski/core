package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.model.SupervisorDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupervisorMapper {

    Supervisor mapToEntity(SupervisorDTO dto);

    @Mapping(target = "userData", ignore = true)
    SupervisorDTO mapToDto(Supervisor entity);

    @Mapping(target = "userData", ignore = true)
    List<SupervisorDTO> mapToDtoList(List<Supervisor> entityList);
}