package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.Semester;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "CRITERIA_GROUP")
public class CriteriaGroup extends AbstractEntity {

    @NotNull
    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "CRITERIA_GROUP_ID")
    private List<Criterion> criteria = new ArrayList<>();

    private Double criteriaGroupGradeWeight;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    private EvaluationCardTemplate evaluationCardTemplate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CriteriaGroup that = (CriteriaGroup) o;
        return Objects.equals(name, that.name) && Objects.equals(criteria, that.criteria) && Objects.equals(criteriaGroupGradeWeight, that.criteriaGroupGradeWeight) && semester == that.semester && Objects.equals(evaluationCardTemplate, that.evaluationCardTemplate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, criteria, criteriaGroupGradeWeight, semester, evaluationCardTemplate);
    }
}
