package pl.edu.amu.wmi.model.scheduleconfig;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TimeRangeDTO {

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="[HH:mm][H:mm]")
    private LocalTime start;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="[HH:mm][H:mm]")
    private LocalTime end;

}
