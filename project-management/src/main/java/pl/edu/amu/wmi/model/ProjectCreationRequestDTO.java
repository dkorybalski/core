package pl.edu.amu.wmi.model;

import lombok.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;

import java.util.Set;

@Data
@NoArgsConstructor
public class ProjectCreationRequestDTO {

    private String name;

    private String description;

    private Set<String> technologies;

    private AcceptanceStatus acceptanceStatus;

    private Long supervisorId;

    private String studyYear;
}
