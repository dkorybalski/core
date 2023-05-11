package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "INSTRUCTOR")
public class Instructor extends AbstractEntity {

    @NotNull
    @Embedded
    private UserData userData;

    @OneToMany
    @JoinColumn(name = "INSTRUCTOR_ID")
    Set<Project> projects;

    private Integer maxNumberOfProjects;

    private String studyYear;

}
