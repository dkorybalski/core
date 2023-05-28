package pl.edu.amu.wmi.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
public class ProjectDetailsDTO {

    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotEmpty
    private Set<String> technologies;

    private boolean accepted;

    @NotNull
    private SupervisorDTO supervisor;

    @NotEmpty
    private Set<StudentDTO> students;

    private String admin;

//    ExternalLinkDTO
}
