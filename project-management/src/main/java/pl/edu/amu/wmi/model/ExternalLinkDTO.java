package pl.edu.amu.wmi.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalLinkDTO {

    @NotNull
    private Long id;

    private String url;

    private ExternalLinkDefinitionDTO externalLinkDefinition;

}

