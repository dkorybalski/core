package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "EVALUATION_CARD_TEMPLATE")
public class EvaluationCardTemplate extends AbstractEntity {

    private String studyYear;

    /**
     * Minimum percentage of points required for admission to defence in first semester
     */
    private Double minPointsThresholdFirstSemester;

    /**
     * Minimum percentage of points required for admission to defence in second semester
     */
    private Double minPointsThresholdSecondSemester;

    @OneToMany(mappedBy = "evaluationCardTemplate")
    @Where(clause = "semester = 'FIRST'")
    List<CriteriaSection> criteriaSectionsFirstSemester = new ArrayList<>();

    @OneToMany(mappedBy = "evaluationCardTemplate")
    @Where(clause = "semester = 'SECOND'")
    List<CriteriaSection> criteriaSectionsSecondSemester = new ArrayList<>();

    public void addCriteriaSectionForFirstSemester(CriteriaSection criteriaSection) {
        criteriaSectionsFirstSemester.add(criteriaSection);
        criteriaSection.setEvaluationCardTemplate(this);
    }

    public void removeCriteriaSectionForFirstSemester(CriteriaSection criteriaSection) {
        criteriaSectionsFirstSemester.remove(criteriaSection);
        criteriaSection.setEvaluationCardTemplate(null);
    }

    public void addCriteriaSectionForSecondSemester(CriteriaSection criteriaSection) {
        criteriaSectionsSecondSemester.add(criteriaSection);
        criteriaSection.setEvaluationCardTemplate(this);
    }

    public void removeCriteriaSectionForSecondSemester(CriteriaSection criteriaSection) {
        criteriaSectionsSecondSemester.remove(criteriaSection);
        criteriaSection.setEvaluationCardTemplate(null);
    }

}
