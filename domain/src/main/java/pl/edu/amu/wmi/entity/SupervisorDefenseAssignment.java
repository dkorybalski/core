package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;

@Getter
@Setter
@Entity
@Table(name = "SUPERVISOR_DEFENSE_ASSIGNMENT")
public class SupervisorDefenseAssignment extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private DefenseTimeSlot defenseTimeSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    private Supervisor supervisor;

    /**
     * is supervisor available in the timeslot
     */
    private boolean isAvailable;

    /**
     * is a supervisor a chairperson of the committee
     */
    private boolean isChairperson;

    @Enumerated(EnumType.STRING)
    private CommitteeIdentifier committeeIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProjectDefense projectDefense;

    // TODO: 12/2/2023 this field should be taken from SupervisorDefenseAssignment entity of chairperson and copied to other commitee members during save
    private String classroom;

}
