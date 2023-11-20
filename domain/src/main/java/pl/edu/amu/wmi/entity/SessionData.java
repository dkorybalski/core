package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SESSION_DATA")
public class SessionData extends BaseAbstractEntity {

    @OneToOne
    @JoinColumn(name = "USER_DATA_ID")
    private UserData userData;

    @Column
    private String actualStudyYear;
}
