package pl.edu.amu.wmi.service.project;

import pl.edu.amu.wmi.entity.Project;
import pl.edu.amu.wmi.entity.Supervisor;
import pl.edu.amu.wmi.entity.UserData;
import pl.edu.amu.wmi.enumerations.UserRole;

public interface ProjectMemberService {

    // TODO: 11/23/2023 what with study years and roles
    UserRole getUserRoleByUserIndex(String indexNumber);

    boolean isUserRoleCoordinator(String indexNumber);

    UserData findUserDataByIndexNumber(String indexNumber);

    boolean isStudentAMemberOfProject(String indexNumber, Project project);

    boolean isUserAProjectSupervisor(Supervisor supervisor, String indexNumber);

    boolean isStudentAnAdminOfTheProject(String userIndexNumber, Long projectId);

}
