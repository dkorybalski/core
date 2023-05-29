package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.ProjectRole;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "STUDENT_PROJECT")
public class StudentProject {

    @EmbeddedId
    private StudentProjectId id;

    @ManyToOne
    @MapsId("studentId")
    private Student student;

    @ManyToOne
    @MapsId("projectId")
    private Project project;

    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;

    private boolean isProjectAdmin;

    private boolean isProjectConfirmed;

    private StudentProject() {
    }

    public StudentProject(Student student, Project project) {
        this.student = student;
        this.project = project;
        this.id = new StudentProjectId(student.getId(), project.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentProject that = (StudentProject) o;
        return Objects.equals(student, that.student) && Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(student, project);
    }
}
