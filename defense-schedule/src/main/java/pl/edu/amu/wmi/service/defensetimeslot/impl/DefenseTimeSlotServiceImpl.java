package pl.edu.amu.wmi.service.defensetimeslot.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.amu.wmi.dao.DefenseScheduleConfigDAO;
import pl.edu.amu.wmi.dao.DefenseTimeSlotDAO;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.entity.DefenseTimeSlot;
import pl.edu.amu.wmi.exception.BusinessException;
import pl.edu.amu.wmi.service.defensetimeslot.DefenseTimeSlotService;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

import static pl.edu.amu.wmi.util.CommonDateUtils.getDefenseDays;

@Service
@Slf4j
public class DefenseTimeSlotServiceImpl implements DefenseTimeSlotService {

    private final DefenseTimeSlotDAO defenseTimeSlotDAO;
    private final DefenseScheduleConfigDAO defenseScheduleConfigDAO;

    public DefenseTimeSlotServiceImpl(DefenseTimeSlotDAO defenseTimeSlotDAO, DefenseScheduleConfigDAO defenseScheduleConfigDAO) {
        this.defenseTimeSlotDAO = defenseTimeSlotDAO;
        this.defenseScheduleConfigDAO = defenseScheduleConfigDAO;
    }

    /**
     * Create all defense timeslots for the selected configuration.
     */
    @Override
    @Transactional
    public void createDefenseTimeSlots(String studyYear, Long defenseScheduleConfigId) {
        DefenseScheduleConfig defenseScheduleConfig = defenseScheduleConfigDAO.findById(defenseScheduleConfigId)
                .orElseThrow(() -> new BusinessException(MessageFormat.format("Defense schedule config with id: {0} not found", defenseScheduleConfigId)));

        LocalDate startDay = defenseScheduleConfig.getStartDate();
        LocalDate endDay = defenseScheduleConfig.getEndDate();
        LocalTime startTime = defenseScheduleConfig.getStartTime();
        LocalTime endTime = defenseScheduleConfig.getEndTime();
        Integer defenseSlotDuration = defenseScheduleConfig.getDefenseDuration();

        List<LocalDate> defenseDays = getDefenseDays(startDay, endDay);
        List<LocalTime> defenseHours = getDefenseHours(startTime, endTime, defenseSlotDuration);

        defenseDays.forEach(day -> {
            defenseHours.forEach(hour -> {
                DefenseTimeSlot defenseTimeSlot = createSingleTimeSlot(day, hour, hour.plusMinutes(defenseSlotDuration), defenseSlotDuration, studyYear, defenseScheduleConfig);
                defenseTimeSlotDAO.save(defenseTimeSlot);
            });
            log.info("Defense timeslots were created for day: {}", day.toString());
        });
    }

    /**
     * Calculate defense hours per day based on start time, end time and defense duration.
     */
    private List<LocalTime> getDefenseHours(LocalTime startTime, LocalTime endTime, Integer defenseSlotDuration) {
        long defenseDayLength = Duration.between(startTime, endTime).toMinutes();
        long numOfSlotsPerDay = defenseDayLength / defenseSlotDuration;

        return IntStream.iterate(0, i -> i + defenseSlotDuration)
                .limit(numOfSlotsPerDay)
                .mapToObj(startTime::plusMinutes)
                .toList();
    }

    private DefenseTimeSlot createSingleTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime, Integer duration, String studyYear, DefenseScheduleConfig defenseScheduleConfig) {
        DefenseTimeSlot defenseTimeSlot = new DefenseTimeSlot();
        defenseTimeSlot.setDate(date);
        defenseTimeSlot.setStartTime(startTime);
        defenseTimeSlot.setEndTime(endTime);
        defenseTimeSlot.setDuration(duration);
        defenseTimeSlot.setStudyYear(studyYear);
        defenseTimeSlot.setDefenseScheduleConfig(defenseScheduleConfig);
        return defenseTimeSlot;
    }

    @Override
    public List<DefenseTimeSlot> getAllTimeSlotsForDefenseConfig(Long defenseScheduleConfigId) {
        return defenseTimeSlotDAO.findAllByDefenseScheduleConfig_Id(defenseScheduleConfigId);
    }

    @Override
    @Transactional
    public void deleteAllConnectedWithDefenseScheduleConfig(Long defenseScheduleConfigId) {
        defenseTimeSlotDAO.deleteAllByDefenseScheduleConfig_Id(defenseScheduleConfigId);
        log.info("All defense time slots have been deleted for defense schedule config: {}", defenseScheduleConfigId);
    }

}

