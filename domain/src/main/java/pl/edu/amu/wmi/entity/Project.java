package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "PROJECT")
public class Project extends AbstractEntity {

    private String name;

    private String description;

    @OneToMany(mappedBy = "project")
    private Set<Student> students;

    private Set<String> technologies;

    private AcceptanceStatus acceptanceStatus;

    @ManyToOne
    @JoinColumn(name = "INSTRUCTOR_ID")
    private Instructor instructor;

    @OneToMany
    @JoinColumn(name = "PROJECT_ID")
    private Set<ExternalLink> externalLinks;

    @ManyToOne
    @JoinColumn(name = "STUDY_YEAR_ID")
    private StudyYear studyYear;

}
