package pl.edu.amu.wmi.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO {

    private String indexNumber;

    private String name;

    /**
     * role assigned for actualYear
     */
    private String role;

    /**
     * study year for which the data: projects, accepted projects and role are displayed
     */
    private String actualYear;

    /**
     * list of all study years connected with user
     */
    private List<String> studyYears = new ArrayList<>();

    /**
     * user projects connected with actualYear
     */
    private List<String> projects = new ArrayList<>();

    /**
     * accepted projects in actual year
     */
    private List<String> acceptedProjects = new ArrayList<>();

}
