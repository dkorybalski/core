package pl.edu.amu.wmi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ExternalLinkDefinitionDTO {

    private Long id;

    private String name;

    private String columnHeader;

    private LocalDate deadline;

}
