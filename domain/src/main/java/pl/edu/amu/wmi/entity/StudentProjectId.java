package pl.edu.amu.wmi.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class StudentProjectId implements Serializable {

    private Long studentId;

    private Long projectId;

    // TODO: 5/28/2023 check if it will work with lombok annotations
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentProjectId that = (StudentProjectId) o;
        return Objects.equals(studentId, that.studentId) && Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, projectId);
    }
}
