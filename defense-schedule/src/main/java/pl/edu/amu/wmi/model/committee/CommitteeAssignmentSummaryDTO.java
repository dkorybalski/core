package pl.edu.amu.wmi.model.committee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitteeAssignmentSummaryDTO {

    private List<SupervisorStatisticsDTO> statistics;

    private List<ProjectDefenseDTO> defenses;

}
