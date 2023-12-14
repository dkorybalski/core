package pl.edu.amu.wmi.model.committee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChairpersonAssignmentDTO {

    private String chairpersonId;

    private String chairpersonInitials;

    private String classroom;

    private CommitteeIdentifier committeeIdentifier;

    private String date;
}
