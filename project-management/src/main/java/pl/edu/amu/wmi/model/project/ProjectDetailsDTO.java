package pl.edu.amu.wmi.model.project;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;

import java.util.List;
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

    private boolean confirmed;

    private boolean accepted;

    @NotNull
    private SupervisorDTO supervisor;

    @NotEmpty
    private List<StudentDTO> students;

    private String admin;

    private Set<ExternalLinkDTO> externalLinks;

}
