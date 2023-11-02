package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.CriterionCategory;

@Getter
@Setter
@Entity
@Table(name = "SCORING_CRITERIA")
public class ScoringCriteria extends AbstractEntity {

    @NotNull
    private Integer points;

    @Column(length = 2000)
    private String description;

    private boolean isDisqualifying;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CriterionCategory criterionCategory;

}
