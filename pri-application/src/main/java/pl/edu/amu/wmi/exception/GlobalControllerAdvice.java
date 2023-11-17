package pl.edu.amu.wmi.exception;

import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorInfo businessExceptionHandler(final Exception exception) {
        log.error("Business exception occurred", exception);
        return new ErrorInfo(exception.getMessage(), 500);
    }

    @ResponseBody
    @ExceptionHandler(TokenRefreshException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorInfo tokenRefreshExceptionHandler(final Exception exception) {
        log.error("TokenRefreshException exception occurred", exception);
        return new ErrorInfo(exception.getMessage(), 403);
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorInfo accessDeniedExceptionHandler(final AccessDeniedException exception) {
        log.error("Access denied", exception);
        return new ErrorInfo(exception.getMessage(), 403);
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorInfo illegalArgumentExceptionHandler(final IllegalArgumentException exception) {
        log.error("IllegalArgumentException exception occured", exception);
        return new ErrorInfo(exception.getMessage(), 400);
    }

//    todo validate the error code
    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorInfo exceptionHandler(final Exception exception) {
        log.error("Unexpected exception occurred", exception);
        return new ErrorInfo(exception.getMessage(), 500);
    }

}
