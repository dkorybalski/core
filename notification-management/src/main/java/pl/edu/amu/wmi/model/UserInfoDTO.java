package pl.edu.amu.wmi.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserInfoDTO {

    private String firstName;

    private String lastName;

    private String email;

}
