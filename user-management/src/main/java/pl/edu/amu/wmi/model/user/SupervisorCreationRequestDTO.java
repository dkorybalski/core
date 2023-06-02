package pl.edu.amu.wmi.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupervisorCreationRequestDTO {

    private String surname;

    private String name;

    private String email;

    private String indexNumber;

    private Integer groupNumber;

}
