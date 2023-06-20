package pl.edu.amu.wmi.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StudentCreationRequestDTO {

    private String name;

    private String surname;

    private String indexNumber;

    private String email;

    private String pesel;

}
