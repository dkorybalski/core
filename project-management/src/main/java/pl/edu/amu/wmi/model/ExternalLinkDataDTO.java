package pl.edu.amu.wmi.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class ExternalLinkDataDTO {

    // TODO: Add fields validation
    @NotNull
    private Long projectId;

    private String projectName;

    private SupervisorDTO supervisor;

    @NotNull
    private Set<ExternalLinkDTO> externalLinks;

}
