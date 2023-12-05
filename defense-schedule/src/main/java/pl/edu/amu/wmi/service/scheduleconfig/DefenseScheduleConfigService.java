package pl.edu.amu.wmi.service.scheduleconfig;

import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleConfigDTO;

public interface DefenseScheduleConfigService {

    void createDefenseScheduleConfig(String studyYear, DefenseScheduleConfigDTO defenseScheduleConfig);

}
