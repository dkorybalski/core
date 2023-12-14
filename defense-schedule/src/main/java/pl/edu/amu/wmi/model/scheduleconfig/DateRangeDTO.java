package pl.edu.amu.wmi.model.scheduleconfig;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DateRangeDTO {

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd.MM.yyyy")
    private LocalDate start;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd.MM.yyyy")
    private LocalDate end;

}
