package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.ProjectRole;

@Getter
@Setter
@Entity
@Table(name = "STUDENT")
public class Student extends AbstractEntity {

    @NotNull
    @Embedded
    private UserData userData;

    private String indexNumber;

    private ProjectRole projectRole;

    private boolean isProjectAdmin;

    private boolean isProjectConfirmed;

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    private String studyYear;
}
