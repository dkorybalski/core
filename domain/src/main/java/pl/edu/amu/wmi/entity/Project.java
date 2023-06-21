package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.ProjectRole;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "PROJECT")
public class Project extends AbstractEntity {

    private String name;

    private String description;

    @OneToMany(mappedBy = "confirmedProject")
    private Set<Student> students;

    private Set<String> technologies;

    @Enumerated(EnumType.STRING)
    private AcceptanceStatus acceptanceStatus;

    @ManyToOne
    @JoinColumn(name = "SUPERVISOR_ID")
    private Supervisor supervisor;

    // TODO: 6/21/2023 validate cascade type | liquibase changes?
    @OneToMany(
            cascade = CascadeType.REMOVE
    )
    @JoinColumn(name = "PROJECT_ID")
    private Set<ExternalLink> externalLinks = new HashSet<>();

    @ManyToOne
    @JoinColumn(
            name = "STUDY_YEAR",
            referencedColumnName = "STUDY_YEAR"
    )
    private StudyYear studyYear;

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL
    )
    private Set<StudentProject> assignedStudents = new HashSet<>();

    public void addStudent(Student student, ProjectRole projectRole, boolean isProjectAdmin) {
        StudentProject studentProject = new StudentProject(student, this);
        studentProject.setProjectRole(projectRole);
        studentProject.setProjectAdmin(isProjectAdmin);
        assignedStudents.add(studentProject);
//        student.getAssignedProjects().add(studentProject);
    }

    public void addExternalLink(ExternalLink externalLink) {
        externalLinks.add(externalLink);
    }

}