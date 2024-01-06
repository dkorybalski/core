package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.EvaluationPhase;
import pl.edu.amu.wmi.enumerations.EvaluationStatus;
import pl.edu.amu.wmi.enumerations.Semester;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EVALUATION_CARD")
public class EvaluationCard extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    @Enumerated(EnumType.STRING)
    private EvaluationPhase evaluationPhase;

    @Enumerated(EnumType.STRING)
    private EvaluationStatus evaluationStatus;

    @Column(name = "TOTAL_POINTS")
    private Double totalPoints;

    @Column(columnDefinition = "boolean default true")
    private boolean isDisqualified = true;

    private boolean isApprovedForDefense;

    @ManyToOne
    @JoinColumn(name = "EVALUATION_CARD_TEMPLATE_ID")
    private EvaluationCardTemplate evaluationCardTemplate;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "EVALUATION_CARD_ID")
    private List<Grade> grades = new ArrayList<>();

    @Column(name = "FINAL_GRADE")
    private Double finalGrade;

    @Column(columnDefinition = "boolean default false")
    private boolean isActive = false;
}
