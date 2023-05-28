package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.ProjectRole;

import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "STUDENT")
public class Student extends AbstractEntity {

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_DATA_ID")
    private UserData userData;

    private String pesel;

    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;

    private boolean isProjectAdmin;

    private boolean isProjectConfirmed;

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID")
    private Project confirmedProject;

    @ManyToMany
    @JoinTable(
            name = "STUDENT_ASSIGNED_PROJECTS",
            joinColumns = @JoinColumn(name = "STUDENT_ID"),
            inverseJoinColumns = @JoinColumn(name = "PROJECT_ID")
    )
    private Set<Project> allAssignedProjects;

}
