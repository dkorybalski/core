package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name = "SUPERVISOR")
public class Supervisor extends AbstractEntity {

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_DATA_ID")
    private UserData userData;

    @OneToMany
    @JoinColumn(name = "SUPERVISOR_ID")
    Set<Project> projects;

    private Integer maxNumberOfProjects;

    private Integer groupNumber;

}
