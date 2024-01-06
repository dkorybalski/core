package pl.edu.amu.wmi.service.defensetimeslot;

import pl.edu.amu.wmi.entity.DefenseTimeSlot;

import java.util.List;

public interface DefenseTimeSlotService {

    void createDefenseTimeSlots(String studyYear, Long defenseScheduleConfigId);

    List<DefenseTimeSlot> getAllTimeSlotsForDefenseConfig(Long defenseScheduleConfigId);

    void deleteAllConnectedWithDefenseScheduleConfig(Long defenseScheduleConfigId);
}
