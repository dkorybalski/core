package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "GRADE")
public class Grade extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "CRITERIA_GROUP_ID")
    private CriteriaGroup criteriaGroup;

    private Double points;

    private Double pointsWithWeight;

    private boolean isDisqualifying;

}
