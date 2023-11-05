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
@Table(name = "CRITERIA_SECTION")
public class CriteriaSection extends AbstractEntity {

    @NotNull
    private String name;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "CRITERIA_SECTION_ID")
    private List<CriteriaGroup> criteriaGroups = new ArrayList<>();

    private Double criteriaSectionGradeWeight;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    private EvaluationCardTemplate evaluationCardTemplate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CriteriaSection that = (CriteriaSection) o;
        return Objects.equals(name, that.name) && Objects.equals(criteriaGroups, that.criteriaGroups) && Objects.equals(criteriaSectionGradeWeight, that.criteriaSectionGradeWeight) && semester == that.semester && Objects.equals(evaluationCardTemplate, that.evaluationCardTemplate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, criteriaGroups, criteriaSectionGradeWeight, semester, evaluationCardTemplate);
    }
}
