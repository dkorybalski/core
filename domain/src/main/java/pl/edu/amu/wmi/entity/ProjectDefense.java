package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "PROJECT_DEFENSE")
public class ProjectDefense extends AbstractEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @OneToMany(mappedBy = "projectDefense", cascade = CascadeType.MERGE)
    private List<SupervisorDefenseAssignment> supervisorDefenseAssignments = new ArrayList<>();

    private String studyYear;

    @Transient
    private SupervisorDefenseAssignment chairpersonDefenseAssignment;

    @PostLoad
    public void fetchChairpersonDefenseAssignment() {
        this.chairpersonDefenseAssignment = supervisorDefenseAssignments.stream()
                .filter(defenseAssignment -> Objects.equals(Boolean.TRUE, defenseAssignment.isChairperson()))
                .findFirst().orElse(null);
    }

    // classroom, timeslot and group identifier are taken from chairperson defense assignment object
    public String getClassroom() {
        return Objects.nonNull(this.chairpersonDefenseAssignment) ? this.chairpersonDefenseAssignment.getClassroom() : null;
    }

    public DefenseTimeSlot getDefenseTimeslot() {
        return Objects.nonNull(this.chairpersonDefenseAssignment) ? this.chairpersonDefenseAssignment.getDefenseTimeSlot() : null;
    }

    public CommitteeIdentifier getCommitteeIdentifier() {
        return Objects.nonNull(this.chairpersonDefenseAssignment) ? this.chairpersonDefenseAssignment.getCommitteeIdentifier() : null;
    }

    public void addSupervisorDefenseAssignments(List<SupervisorDefenseAssignment> supervisorDefenseAssignments) {
        this.setSupervisorDefenseAssignments(supervisorDefenseAssignments);
        supervisorDefenseAssignments.forEach(supervisorDefenseAssignment -> supervisorDefenseAssignment.setProjectDefense(this));
    }
}
