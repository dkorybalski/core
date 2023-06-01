package pl.edu.amu.wmi.exception;

import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    @ResponseBody
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorInfo conflictExceptionHandler(final Exception exception) {
        log.error("DuplicateKeyException occurred", exception);
        return new ErrorInfo(exception.getMessage(), 409);
    }

    @ResponseBody
    @ExceptionHandler(CsvException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorInfo globalExceptionHandler(final Exception exception) {
        log.error("A CSV exception occurred", exception);
        return new ErrorInfo(exception.getMessage(), 400);
    }

}
