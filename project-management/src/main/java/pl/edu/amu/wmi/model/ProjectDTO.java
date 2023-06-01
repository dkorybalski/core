package pl.edu.amu.wmi.model;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class ProjectDTO {

    private Long id;

    private String name;

    private SupervisorDTO supervisor;

    private boolean accepted;

}
