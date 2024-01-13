package pl.edu.amu.wmi.service.defensetimeslot;

import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;

import java.time.LocalDate;
import java.util.List;

public interface DefenseTimeSlotService {

    void createDefenseTimeSlots(String studyYear, Long defenseScheduleConfigId);

    List<DefenseTimeSlot> getAllTimeSlotsForDefenseConfig(Long defenseScheduleConfigId);

    void deleteAllConnectedWithDefenseScheduleConfig(Long defenseScheduleConfigId);

    void createDefenseTimeSlotsForASingleDefenseDay(String studyYear, DefenseScheduleConfig defenseScheduleConfig, LocalDate date);

    List<DefenseTimeSlot> getAllTimeSlotsForDefenseConfigAndDate(Long defenseScheduleConfigId, LocalDate date);
}
