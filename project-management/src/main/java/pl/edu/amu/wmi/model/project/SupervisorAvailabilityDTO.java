package pl.edu.amu.wmi.model.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SupervisorAvailabilityDTO {

    private SupervisorDTO supervisor;

    private int assigned;

    private int max;

}
