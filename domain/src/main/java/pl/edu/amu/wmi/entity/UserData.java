package pl.edu.amu.wmi.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class UserData {

    private String firstName;

    private String lastName;

    private String email;

}
