package pl.edu.amu.wmi.service;

import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;
import pl.edu.amu.wmi.model.UserRoleType;

public interface ProjectMemberService {

    // TODO: 11/23/2023 what with study years and roles
    /**
     * Returns the role of the user based on the parameter userRoleType:
     * - BASIC - returns the basic role of a user (SUPERVISOR or STUDENT), even, if the user has a role with special permissions
     * - SPECIAL - if the user has a role with special permissions (COORDINATOR or PROJECT_ADMIN), returns this role, otherwise returns
     * the basic role (SUPERVISOR or STUDENT)
     *
     * @param indexNumber  - index of a user
     * @param userRoleType - the type of searched role (basic or special)
     * @return the role of a user
     */
    UserRole getUserRoleByUserIndex(String indexNumber, UserRoleType userRoleType);

    boolean isUserRoleCoordinator(String indexNumber);

    UserData findUserDataByIndexNumber(String indexNumber);

    boolean isStudentAMemberOfProject(String indexNumber, Project project);

    boolean isUserAProjectSupervisor(Supervisor supervisor, String indexNumber);

    boolean isStudentAnAdminOfTheProject(String userIndexNumber, Long projectId);

}
