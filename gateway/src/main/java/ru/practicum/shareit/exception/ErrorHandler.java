package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.Objects;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        return new ErrorResponse(e.getConstraintViolations().stream()
                .map(v -> String.format("%s%s", v.getMessage(), v.getInvalidValue())).findFirst().orElse(null));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ErrorResponse(String.format("Ошибка с полем %s: %s",
                Objects.requireNonNull(e.getFieldError()).getField(),
                e.getFieldError().getDefaultMessage()
        ));
    }

    @Getter
    @RequiredArgsConstructor
    public static class ErrorResponse {
        private final String error;
    }
}
