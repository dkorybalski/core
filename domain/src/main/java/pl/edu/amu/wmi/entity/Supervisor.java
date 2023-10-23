package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "SUPERVISOR")
public class Supervisor extends AbstractEntity {

    // TODO: 8/21/2023 verify cascade
    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_DATA_ID")
    private UserData userData;

    @OneToMany
    @JoinColumn(name = "SUPERVISOR_ID")
    Set<Project> projects;

    private Integer maxNumberOfProjects;

    private Integer groupNumber;

    private String studyYear;

}
