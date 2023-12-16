package pl.edu.amu.wmi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitteeAssignmentCriteria {

    private Long supervisorId;
    private CommitteeIdentifier committeeIdentifier;
    private LocalDate date;
    private String studyYear;
    private Long defenseTimeslotId;
    private List<Long> excludedSupervisorIds;
    private Boolean isChairperson;

}
