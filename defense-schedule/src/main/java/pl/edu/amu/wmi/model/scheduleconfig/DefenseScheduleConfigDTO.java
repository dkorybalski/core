package pl.edu.amu.wmi.model.scheduleconfig;

import lombok.Data;

@Data
public class DefenseScheduleConfigDTO {

    private DateRangeDTO dateRange;

    private Integer slotDuration;

    private TimeRangeDTO timeRange;

}