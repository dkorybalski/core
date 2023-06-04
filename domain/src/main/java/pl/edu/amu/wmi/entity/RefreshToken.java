package pl.edu.amu.wmi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "REFRESH_TOKEN")
public class RefreshToken extends BaseAbstractEntity {

    @OneToOne
    @JoinColumn(name = "USER_DATA_ID")
    private UserData user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

}
