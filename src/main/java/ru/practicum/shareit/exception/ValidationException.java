package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request")
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
