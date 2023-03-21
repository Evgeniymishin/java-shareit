package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ResponseStatus(value= HttpStatus.CONFLICT, reason="Conflict")
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}