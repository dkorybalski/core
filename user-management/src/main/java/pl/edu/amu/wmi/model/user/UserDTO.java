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

    private String role;

    private List<String> studyYears = new ArrayList<>();

    private List<String> projects = new ArrayList<>();

    private List<String> acceptedProjects = new ArrayList<>();

}
