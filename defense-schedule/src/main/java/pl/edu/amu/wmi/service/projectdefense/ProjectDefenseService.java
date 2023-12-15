package pl.edu.amu.wmi.service.projectdefense;

import pl.edu.amu.wmi.entity.SupervisorDefenseAssignment;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefensePatchDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectDefenseSummaryDTO;
import pl.edu.amu.wmi.model.projectdefense.ProjectNameDTO;

import java.util.List;
import java.util.Map;

public interface ProjectDefenseService {

    void createProjectDefense(String studyYear, List<SupervisorDefenseAssignment> committeeMembers);

    /**
     * Returns all project defense slots for a study year with an information, if a slot can be edited by a user
     *
     * @param studyYear - study year that project defense objects are fetched for
     * @param username  - user index - define if user is allowed to edit the project defense slot
     * @return list of {@link ProjectDefenseDTO}
     */
    List<ProjectDefenseDTO> getProjectDefenses(String studyYear, String username);

    /**
     * Returns all project defense slots for a study year to which a projects were assign to
     *
     * @param studyYear - study year that project defense objects are fetched for
     * @return map of {@link ProjectDefenseSummaryDTO} grouped by date
     */
    Map<String, List<ProjectDefenseSummaryDTO>> getProjectDefensesSummary(String studyYear);

    /**
     * Assigns the project to selected project defense slot with additional validation if the change can be performed
     *
     * @param studyYear              - study year that project defense is connected with
     * @param indexNumber            - index number of the user
     * @param projectDefenseId       - id of the project defense slot
     * @param projectDefensePatchDTO - contains the project id (or null value), that should be assigned to the project defense
     */
    void assignProjectToProjectDefense(String studyYear, String indexNumber, Long projectDefenseId, ProjectDefensePatchDTO projectDefensePatchDTO);

    /**
     * Returns a list with project ids, names and related project defenses
     *
     * @param studyYear - study year that project names are connected with
     * @return list of project names
     */
    List<ProjectNameDTO> getProjectNames(String studyYear);

    void deleteProjectDefenses(List<Long> projectDefenseIdsToBeRemoved);
}
