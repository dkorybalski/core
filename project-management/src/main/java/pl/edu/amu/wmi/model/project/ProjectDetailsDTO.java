package pl.edu.amu.wmi.model.project;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.model.externallink.ExternalLinkDTO;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class ProjectDetailsDTO {

    private String id;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotEmpty
    private Set<String> technologies;

    private boolean confirmed;

    private boolean accepted;

    private boolean freezeButtonShown;

    private boolean publishButtonShown;

    private boolean retakeButtonShown;

    @NotNull
    private SupervisorDTO supervisor;

    @NotEmpty
    private List<StudentDTO> students;

    private String admin;

    private Set<ExternalLinkDTO> externalLinks;

}
