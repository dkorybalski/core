package pl.edu.amu.wmi.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StudentDTO {

    private String name;

    private String email;

    private String indexNumber;

    private String role;

}
