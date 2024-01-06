package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    public Grade createACopy() {
        return new Grade(this.criteriaGroup, this.points, this.pointsWithWeight, this.isDisqualifying);
    }
}
