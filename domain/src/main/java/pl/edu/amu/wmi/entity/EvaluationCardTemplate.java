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
    @Where(clause = "semester = 'SEMESTER_I'")
    List<CriteriaGroup> criteriaGroupsFirstSemester = new ArrayList<>();

    @OneToMany(mappedBy = "evaluationCardTemplate")
    @Where(clause = "semester = 'SEMESTER_II'")
    List<CriteriaGroup> criteriaGroupsSecondSemester = new ArrayList<>();

    public void addCriteriaGroupForFirstSemester(CriteriaGroup criteriaGroup) {
        criteriaGroupsFirstSemester.add(criteriaGroup);
        criteriaGroup.setEvaluationCardTemplate(this);
    }

    public void removeCriteriaGroupForFirstSemester(CriteriaGroup criteriaGroup) {
        criteriaGroupsFirstSemester.remove(criteriaGroup);
        criteriaGroup.setEvaluationCardTemplate(null);
    }

    public void addCriteriaGroupForSecondSemester(CriteriaGroup criteriaGroup) {
        criteriaGroupsSecondSemester.add(criteriaGroup);
        criteriaGroup.setEvaluationCardTemplate(this);
    }

    public void removeCriteriaGroupForSecondSemester(CriteriaGroup criteriaGroup) {
        criteriaGroupsSecondSemester.remove(criteriaGroup);
        criteriaGroup.setEvaluationCardTemplate(null);
    }

}
