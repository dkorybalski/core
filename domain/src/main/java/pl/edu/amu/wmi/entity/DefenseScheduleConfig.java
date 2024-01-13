package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.DefensePhase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@Entity
@Table(name = "DEFENSE_SCHEDULE_CONFIG")
public class DefenseScheduleConfig extends AbstractEntity {

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer defenseDuration;

    private String studyYear;

    private boolean isActive;

    private Set<String> additionalDays = new TreeSet<>();

    @Enumerated(EnumType.STRING)
    private DefensePhase defensePhase;
}
