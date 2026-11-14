package dev.angelone.incidentdesk.web;

import dev.angelone.incidentdesk.service.InvalidTransitionException;
import dev.angelone.incidentdesk.service.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ErrorResponse(String error) {}

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(NotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(InvalidTransitionException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse invalidTransition(InvalidTransitionException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationError(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("validation failed");
        return new ErrorResponse(msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse unexpected(Exception ex) {
        return new ErrorResponse("unexpected error: " + ex.getMessage());
    }
}
