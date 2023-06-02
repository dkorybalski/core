package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.model.user.StudentDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    Student mapToEntity(StudentDTO dto);

    @Mapping(target = "userData", ignore = true)
    StudentDTO mapToDto(Student entity);

    @Mapping(target = "userData", ignore = true)
    List<StudentDTO> mapToDtoList(List<Student> entityList);
}
