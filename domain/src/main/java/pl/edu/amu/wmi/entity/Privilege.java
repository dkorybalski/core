package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "PRIVILEGE")
public class Privilege extends BaseAbstractEntity {

    @NotNull
    private String name;

    @ManyToMany(mappedBy = "privileges")
    private Set<Role> roles;

}
