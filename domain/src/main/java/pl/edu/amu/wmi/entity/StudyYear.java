package pl.edu.amu.wmi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pl.edu.amu.wmi.enumerations.StudyType;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "STUDY_YEAR")
public class StudyYear extends AbstractEntity {

    private String year;

    private StudyType studyType;

    private boolean isActive;

    private LocalDate startDate;

    private LocalDate endDate;

    private String tag;

}
