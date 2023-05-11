package pl.edu.amu.wmi.entity;

import jakarta.persistence.Column;
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

    @Column(name = "`YEAR`")
    private String year;

    @Column(name = "`STUDY_TYPE`")
    private StudyType studyType;

    @Column(name = "`IS_ACTIVE`")
    private boolean isActive;

    @Column(name = "`START_DATE`")
    private LocalDate startDate;

    @Column(name = "`END_DATE`")
    private LocalDate endDate;

    @Column(name = "`TAG`")
    private String tag;

}
