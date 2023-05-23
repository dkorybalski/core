package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.model.NewSupervisorDTO;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SupervisorMapper {

    @Mapping(target = "userData.firstName", source = "dto.firstName")
    @Mapping(target = "userData.lastName", source = "dto.lastName")
    @Mapping(target = "userData.email", source = "dto.email")
    Supervisor mapToEntity(NewSupervisorDTO dto, String studyYear);

    default List<Supervisor> mapToEntities(List<NewSupervisorDTO> dtos, String studyYear) {
        List<Supervisor> entities = new ArrayList<>();
        for (NewSupervisorDTO dto : dtos) {
            entities.add(mapToEntity(dto, studyYear));
        }
        return entities;
    }

    NewSupervisorDTO mapToDTO(Supervisor entity);

    List<NewSupervisorDTO> mapToDTOs(List<Supervisor> entities);
}
