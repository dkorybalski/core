package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.Semester;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "CRITERIA_GROUP")
public class CriteriaGroup extends AbstractEntity {

    @NotNull
    @Column(length = 400)
    private String name;

    @ManyToOne
    @JoinColumn(name = "CRITERIA_SECTION_ID")
    private CriteriaSection criteriaSection;

    @NotNull
    private Double gradeWeight;

    @ManyToMany(cascade = CascadeType.MERGE)
    private Set<Criterion> criteria;

    @Transient
    private Semester semester;

}
