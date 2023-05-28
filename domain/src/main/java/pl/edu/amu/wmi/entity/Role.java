package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "ROLE")
public class Role extends BaseAbstractEntity {

    @NotNull
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<UserData> users;

    @ManyToMany
    @JoinTable(
            name = "ROLES_PRIVILEGES",
            joinColumns = @JoinColumn(name = "ROLE_ID"),
            inverseJoinColumns = @JoinColumn(name = "PRIVILEGE_ID")
    )
    private Set<Privilege> privileges;

}
