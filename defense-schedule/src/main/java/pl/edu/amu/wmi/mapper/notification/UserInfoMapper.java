package pl.edu.amu.wmi.mapper.notification;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.Student;
import pl.edu.amu.wmi.model.UserInfoDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserInfoMapper {

    @Mapping(target = "firstName", source = "userData.firstName")
    @Mapping(target = "lastName", source = "userData.lastName")
    @Mapping(target = "email", source = "userData.email")
    UserInfoDTO mapToUserInfo(Student student);

    List<UserInfoDTO> mapToUserInfos(List<Student> students);
}
