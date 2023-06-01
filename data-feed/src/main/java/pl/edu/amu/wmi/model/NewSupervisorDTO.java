package pl.edu.amu.wmi.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewSupervisorDTO {

    @CsvBindByName(column= "nazwisko")
    private String lastName;

    @CsvBindByName(column = "imie")
    private String firstName;

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "nr_albumu")
    private String indexNumber;

    @CsvBindByName(column = "nr_grupy")
    private Integer groupNumber;

}
