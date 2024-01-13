package pl.edu.amu.wmi.service.scheduleconfig;

import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.ProjectDefense;
import pl.edu.amu.wmi.model.scheduleconfig.DefensePhaseDTO;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleConfigDTO;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleModificationDTO;

public interface DefenseScheduleConfigService {

    void createDefenseScheduleConfig(String studyYear, DefenseScheduleConfigDTO defenseScheduleConfig);

    /**
     * Sets the defense phase of the object {@link DefenseScheduleConfig} to DEFENSE_PROJECT_REGISTRATION
     * Additionally, creates objects {@link ProjectDefense} based on schedule created by coordinator
     *
     * @param studyYear study year that defense registration is open for
     */
    DefensePhaseDTO openRegistrationForDefense(String studyYear);

    /**
     * Sets the defense phase of the object {@link DefenseScheduleConfig} to DEFENSE_PROJECT
     *
     * @param studyYear study year that defense registration is closed for
     */
    DefensePhaseDTO closeRegistrationForDefense(String studyYear);

    DefensePhaseDTO getCurrentDefensePhase(String studyYear);

    void deleteActiveScheduleConfig(String studyYear);

    void archiveDefenseScheduleConfig(String studyYear);

    /**
     * Add defense day based on date from dto object to already defined active defense schedule for a study year
     *
     * @param studyYear
     * @param defenseScheduleModificationDTO
     */
    void modifyDefenseSchedule(String studyYear, DefenseScheduleModificationDTO defenseScheduleModificationDTO);
}
