package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import pl.edu.amu.wmi.enumerations.StudyType;

@Getter
@Setter
@Entity
@Table(name = "STUDY_YEAR")
public class StudyYear extends AbstractEntity {

    /**
     * it is a combination of study type and year, e.g. PART_TIME#2023
     */
    @Column(name = "STUDY_YEAR")
    @NaturalId
    private String studyYear;

    @Column(name = "`YEAR`")
    private String year;

    @Column(name = "STUDY_TYPE")
    @Enumerated(EnumType.STRING)
    private StudyType studyType;

    @Column(name = "IS_ACTIVE")
    private boolean isActive;

    /**
     * field: CDYD_KOD, e.g. value: 2022/SZ
     */
    private String firstSemesterCode;

    /**
     * field: CDYD_KOD, e.g. value: 2022/SZ
     */
    private String secondSemesterCode;

    /**
     * field: PRZ_KOD, e.g. value: 06-DPRILI0
     */
    private String subjectCode;

    /**
     * field: TZAJ_KOD, e.g. value: LAB
     */
    private String subjectType;

}
