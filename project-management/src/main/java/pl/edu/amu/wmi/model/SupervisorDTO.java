package pl.edu.amu.wmi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SupervisorDTO {

    // TODO: 5/28/2023 modify DTO after data feed adjustments
    private Long id;

    private UserDataDTO userData;
}
