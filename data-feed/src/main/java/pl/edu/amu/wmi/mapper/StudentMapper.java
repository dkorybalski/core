package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.model.NewStudentDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "userData.firstName", source = "dto.firstName")
    @Mapping(target = "userData.lastName", source = "dto.lastName")
    @Mapping(target = "userData.email", source = "dto.email")
    @Mapping(target = "userData.indexNumber", source = "dto.indexNumber")
    Student mapToEntity(NewStudentDTO dto);

    List<Student> mapToEntities(List<NewStudentDTO> dtos);

    @Mapping(target = "firstName", source = "userData.firstName")
    @Mapping(target = "lastName", source = "userData.lastName")
    @Mapping(target = "email", source = "userData.email")
    NewStudentDTO mapToDTO(Student entity);

    List<NewStudentDTO> mapToDTOs(List<Student> entities);

}
