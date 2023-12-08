package pl.edu.amu.wmi.service.projectdefense;

import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.enumerations.CommitteeIdentifier;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;

import java.util.List;
import java.util.Map;

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

    /**
     * Returns all project defense slots for a study year with an information, if a slot can be edited by a user
     *
     * @param studyYear - study year that project defense objects are fetched for
     * @param username  - user index - define if user is allowed to edit the project defense slot
     * @return map of {@link ProjectDefenseDTO} grouped by date
     */
    Map<String, List<ProjectDefenseDTO>> getProjectDefenses(String studyYear, String username);
}
