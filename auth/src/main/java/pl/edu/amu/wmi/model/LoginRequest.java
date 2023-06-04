package pl.edu.amu.wmi.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    private String login;

    @NotBlank
    private String password;

}
