package pl.edu.amu.wmi.service.project;

import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;

public interface ProjectMemberService {

    // TODO: 11/23/2023 what with study years and roles
    UserRole getUserRoleByUserIndex(String indexNumber);

    boolean isUserRoleCoordinator(String indexNumber);

    UserData findUserDataByIndexNumber(String indexNumber);

}
