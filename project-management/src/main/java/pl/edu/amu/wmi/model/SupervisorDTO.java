package pl.edu.amu.wmi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SupervisorDTO {

    private Long id;

    private UserDataDTO userData;
}
