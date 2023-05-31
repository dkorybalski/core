package pl.edu.amu.wmi.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ErrorInfo {

    private Instant date;

    private int httpStatusNumber;

    private String errorMessage;

    public ErrorInfo(final String errorMessage, int httpStatusNumber) {
        this.date = Instant.now();
        this.errorMessage = errorMessage;
        this.httpStatusNumber = httpStatusNumber;
    }

}
