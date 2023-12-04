package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.edu.amu.wmi.enumerations.AcceptanceStatus;
import pl.edu.amu.wmi.enumerations.ProjectRole;

import java.util.*;

@Slf4j
@Getter
@Setter
@Entity
@Table(name = "PROJECT")
public class Project extends AbstractEntity {

    private String name;

    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "confirmedProject")
    private Set<Student> students;

    private Set<String> technologies;

    @Enumerated(EnumType.STRING)
    private AcceptanceStatus acceptanceStatus;

    @ManyToOne
    @JoinColumn(name = "SUPERVISOR_ID")
    private Supervisor supervisor;

    // TODO: 6/21/2023 validate cascade type | liquibase changes?
    @OneToMany(
            cascade = CascadeType.REMOVE
    )
    @JoinColumn(name = "PROJECT_ID")
    private Set<ExternalLink> externalLinks = new HashSet<>();

    @ManyToOne
    @JoinColumn(
            name = "STUDY_YEAR",
            referencedColumnName = "STUDY_YEAR"
    )
    private StudyYear studyYear;

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.REMOVE
    )
    private List<EvaluationCard> evaluationCards = new ArrayList<>();

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL
    )
    private Set<StudentProject> assignedStudents = new HashSet<>();

    // TODO: 12/4/2023 check if this cascade type is sufficient
    @OneToOne(
            mappedBy = "project",
            cascade = CascadeType.MERGE,
            fetch = FetchType.LAZY
    )
    private ProjectDefense projectDefense;

    public void addStudent(Student student, ProjectRole projectRole, boolean isProjectAdmin) {
        StudentProject studentProject = new StudentProject(student, this);
        studentProject.setProjectRole(projectRole);
        studentProject.setProjectAdmin(isProjectAdmin);
        assignedStudents.add(studentProject);
//        student.getAssignedProjects().add(studentProject);
    }

    public void removeStudentProject(Set<StudentProject> studentProjectSet) {
        this.assignedStudents.removeAll(studentProjectSet);
    }

    public void addEvaluationCard(EvaluationCard evaluationCard) {
        this.evaluationCards.add(evaluationCard);
        evaluationCard.setProject(this);
    }

    public void setProjectDefense(ProjectDefense projectDefense) {
        // TODO: 12/4/2023 verify if it works correctly
        if (Objects.isNull(projectDefense)) {
            if (Objects.nonNull(this.projectDefense)) {
                this.projectDefense.setProject(null);
            }
        } else {
            projectDefense.setProject(this);
        }
        this.projectDefense = projectDefense;
//        this.setProjectDefense(projectDefense);
    }
}
