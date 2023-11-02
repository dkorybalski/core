package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "CRITERION")
public class Criterion extends AbstractEntity {

    @NotNull
    private String name;

    @ManyToOne
    @JoinColumn(name = "CRITERIA_GROUP_ID")
    private CriteriaGroup criteriaGroup;

    @NotNull
    private Double gradeWeight;

    @ManyToMany
    private Set<ScoringCriteria> scoringCriteria;

}
