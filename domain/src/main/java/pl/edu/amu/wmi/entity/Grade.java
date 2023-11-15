package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "GRADE")
public class Grade extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "CRITERIA_GROUP_ID")
    private CriteriaGroup criteriaGroup;

    private Integer points;

    private Double pointsWithWeight;

    private boolean isDisqualifying;

    public Grade(CriteriaGroup criteriaGroup) {
        this.criteriaGroup = criteriaGroup;
    }
}
