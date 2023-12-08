package pl.edu.amu.wmi.service.projectdefense;

import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;

public interface ProjectDefenseService {

    /**
     * Creates {@link ProjectDefense} objects for all time slots, where Supervisor Defense Assignment has assigned the
     * {@link CommitteeIdentifier} (for each different identifier different committee is created)
     * Additionally, the validation if committee contains exactly one chairperson is performed.
     *
     * @param defenseScheduleConfigId - id of {@link DefenseScheduleConfig}
     * @param studyYear               - study year that project defense object are created for
     */
    void createProjectDefenses(Long defenseScheduleConfigId, String studyYear);
}
