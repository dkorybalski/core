package pl.edu.amu.wmi.mapper.scheduleconfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.edu.amu.wmi.entity.DefenseScheduleConfig;
import pl.edu.amu.wmi.model.scheduleconfig.DefenseScheduleConfigDTO;


@Mapper(componentModel = "spring")
public interface DefenseScheduleConfigMapper {

    @Mapping(target = "dateRange.start", source = "startDate")
    @Mapping(target = "dateRange.end", source = "endDate")
    @Mapping(target = "slotDuration", source = "defenseDuration")
    @Mapping(target = "timeRange.start", source = "startTime")
    @Mapping(target = "timeRange.end", source = "endTime")
    DefenseScheduleConfigDTO mapToDto(DefenseScheduleConfig entity);


    @Mapping(target = "startDate", source = "dateRange.start")
    @Mapping(target = "endDate", source = "dateRange.end")
    @Mapping(target = "startTime", source = "timeRange.start")
    @Mapping(target = "endTime", source = "timeRange.end")
    @Mapping(target = "defenseDuration", source = "slotDuration")
    @Mapping(target = "studyYear", ignore = true)
    @Mapping(target = "defensePhase", ignore = true)
    DefenseScheduleConfig mapToEntity(DefenseScheduleConfigDTO defenseScheduleConfigDTO);

}
