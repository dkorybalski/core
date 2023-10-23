package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.model.user.UserDTO;

import java.util.List;


@Mapper(componentModel = "spring", imports = List.class)
public interface UserMapper {

    @Mapping(target = "name", expression = "java(entity.getFirstName() +\" \" + entity.getLastName())")
    UserDTO mapToDto(UserData entity);

}
