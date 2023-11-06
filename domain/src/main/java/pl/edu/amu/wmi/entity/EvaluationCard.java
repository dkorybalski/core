package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "EVALUATION_CARD")
public class EvaluationCard {

    @Id
    private Long id;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime creationDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime modificationDate;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    private Project project;

    @Column(name = "TOTAL_POINTS_SEMESTER_I")
    private Double totalPointsFirstSemester;

    @Column(name = "TOTAL_POINTS_SEMESTER_II")
    private Double totalPointsSecondSemester;

    private boolean isDisqualified;

    private boolean isApprovedForDefense;

    @ManyToOne
    @JoinColumn(name = "EVALUATION_CARD_TEMPLATE_ID")
    private EvaluationCardTemplate evaluationCardTemplate;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "EVALUATION_CARD_ID")
    private List<Grade> grades = new ArrayList<>();

    @Column(name = "FINAL_GRADE_SEMESTER_I")
    private Double finalGradeFirstSemester;

    @Column(name = "FINAL_GRADE_SEMESTER_II")
    private Double finalGradeSecondSemester;

}
