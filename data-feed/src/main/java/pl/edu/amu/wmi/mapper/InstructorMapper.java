package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Instructor;
import pl.edu.amu.wmi.model.NewInstructorDTO;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface InstructorMapper {

    @Mapping(target = "userData.firstName", source = "dto.firstName")
    @Mapping(target = "userData.lastName", source = "dto.lastName")
    @Mapping(target = "userData.email", source = "dto.email")
    @Mapping(target = "studyYear", source = "studyYear")
    Instructor mapToEntity(NewInstructorDTO dto, String studyYear);

    default List<Instructor> mapToEntities(List<NewInstructorDTO> dtos, String studyYear) {
        List<Instructor> entities = new ArrayList<>();
        for (NewInstructorDTO dto : dtos) {
            entities.add(mapToEntity(dto, studyYear));
        }
        return entities;
    }

    NewInstructorDTO mapToDTO(Instructor entity);

    List<NewInstructorDTO> mapToDTOs(List<Instructor> entities);
}
