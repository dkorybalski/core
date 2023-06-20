package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.model.user.StudentCreationRequestDTO;
import pl.edu.amu.wmi.model.user.StudentDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentUserMapper {

    Student createEntity(StudentDTO dto);

    @Mapping(target = "name", expression = "java(String.format(\"%s %s\", entity.getUserData().getFirstName(), entity.getUserData().getLastName()))")
    @Mapping(target = "email", source = "userData.email")
    @Mapping(target = "indexNumber", source = "userData.indexNumber")
    // TODO: 6/19/2023 should we return a single role or a list ?
    @Mapping(target = "role", expression = "java(entity.getUserData().getRoles().iterator().next().getName().name())")
    StudentDTO mapToDto(Student entity);

    List<StudentDTO> mapToDtoList(List<Student> entityList);

    @Mapping(target = "userData.firstName", source = "dto.name")
    @Mapping(target = "userData.lastName", source = "dto.surname")
    @Mapping(target = "userData.email", source = "dto.email")
    @Mapping(target = "userData.indexNumber", source = "dto.indexNumber")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    Student createEntity(StudentCreationRequestDTO dto);
}
