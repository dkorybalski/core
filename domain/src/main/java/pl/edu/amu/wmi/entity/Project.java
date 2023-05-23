package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;

import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "PROJECT")
public class Project extends AbstractEntity {

    private String name;

    private String description;

    @OneToMany(mappedBy = "confirmedProject")
    private Set<Student> students;

    private Set<String> technologies;

    private AcceptanceStatus acceptanceStatus;

    @ManyToOne
    @JoinColumn(name = "SUPERVISOR_ID")
    private Supervisor supervisor;

    @OneToMany
    @JoinColumn(name = "PROJECT_ID")
    private Set<ExternalLink> externalLinks;

    @ManyToOne
    @JoinColumn(
            name = "STUDY_YEAR",
            referencedColumnName = "STUDY_YEAR"
    )
    private StudyYear studyYear;

}
