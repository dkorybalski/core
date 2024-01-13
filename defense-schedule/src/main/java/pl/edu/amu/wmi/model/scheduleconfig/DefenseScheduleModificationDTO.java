package pl.edu.amu.wmi.model.scheduleconfig;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DefenseScheduleModificationDTO(
        @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd.MM.yyyy")
        @NotNull
        LocalDate date
) {
}
