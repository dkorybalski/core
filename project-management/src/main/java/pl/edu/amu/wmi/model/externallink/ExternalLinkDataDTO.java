package pl.edu.amu.wmi.model.externallink;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.model.project.SupervisorDTO;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalLinkDataDTO {

    @NotNull
    private Long projectId;

    private String projectName;

    private SupervisorDTO supervisor;

    @NotNull
    private Set<ExternalLinkDTO> externalLinks;

}
