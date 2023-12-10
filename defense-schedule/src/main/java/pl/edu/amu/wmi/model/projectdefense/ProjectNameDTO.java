package pl.edu.amu.wmi.model.projectdefense;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectNameDTO {

    Long id;
    String name;
    Long projectDefenseId;

}
