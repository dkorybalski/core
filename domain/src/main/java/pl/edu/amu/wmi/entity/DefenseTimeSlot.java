package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "DEFENSE_TIME_SLOT")
public class DefenseTimeSlot extends AbstractEntity {

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer duration;

    private String studyYear;

    @OneToMany(mappedBy = "defenseTimeSlot")
    private List<SupervisorDefenseAssignment> supervisorDefenseAssignments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private DefenseScheduleConfig defenseScheduleConfig;

    public void addSupervisorDefenseAssignment(SupervisorDefenseAssignment assignment) {
        supervisorDefenseAssignments.add(assignment);
        assignment.setDefenseTimeSlot(this);
    }

}
