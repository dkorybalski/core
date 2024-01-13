package pl.edu.amu.wmi.model.externallink;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ExternalLinkDefinitionDTO {

    private String id;

    private String name;

    private String columnHeader;

    private LocalDate deadline;

}
