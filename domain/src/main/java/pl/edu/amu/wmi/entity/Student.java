package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.ProjectRole;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "STUDENT")
public class Student extends AbstractEntity {

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_DATA_ID")
    private UserData userData;

    private String pesel;

    /**
     * project Role in confirmed project
     */
    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;

    /**
     * is project admin in confirmed project
     */
    private boolean isProjectAdmin;

    /**
     * information if student accepted any of assigned to him / her projects
     */
    private boolean isProjectConfirmed;

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID")
    private Project confirmedProject;

//    @ManyToMany
//    @JoinTable(
//            name = "STUDENT_ASSIGNED_PROJECTS",
//            joinColumns = @JoinColumn(name = "STUDENT_ID"),
//            inverseJoinColumns = @JoinColumn(name = "PROJECT_ID")
//    )
//    private Set<Project> allAssignedProjects;

    @OneToMany(
            mappedBy = "student",
            cascade = CascadeType.ALL
    )
    private Set<StudentProject> assignedProjects = new HashSet<>();

}
