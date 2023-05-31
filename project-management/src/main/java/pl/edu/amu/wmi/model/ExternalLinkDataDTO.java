package pl.edu.amu.wmi.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
