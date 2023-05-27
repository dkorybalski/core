package pl.edu.amu.wmi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDataDTO {

    private String firstName;

    private String lastName;

    private String email;
}
