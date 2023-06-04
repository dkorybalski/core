package pl.edu.amu.wmi.exception;

import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @ResponseBody
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorInfo usernameNotFoundExceptionExceptionHandler(final Exception exception) {
        log.error("UsernameNotFoundException exception occurred", exception);
        return new ErrorInfo(exception.getMessage(), 404);
    }

    @ResponseBody
    @ExceptionHandler(TokenRefreshException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorInfo tokenRefreshExceptionHandler(final Exception exception) {
        log.error("TokenRefreshException exception occurred", exception);
        return new ErrorInfo(exception.getMessage(), 403);
    }

}
