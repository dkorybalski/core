package pl.edu.amu.wmi.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;

import java.util.Set;

@Data
@NoArgsConstructor
public class ProjectCreationResponseDTO {

    private Long id;

    private String name;

    private String description;

    private Set<String> technologies;

    private AcceptanceStatus acceptanceStatus;

    private SupervisorDTO supervisor;

    // Uncomment when ExternalLinkDTO is implemented
    // private Set<ExternalLinkDTO> externalLinks;

    private StudyYearDTO studyYear;
}
