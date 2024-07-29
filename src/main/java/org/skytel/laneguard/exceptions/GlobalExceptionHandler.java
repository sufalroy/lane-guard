package org.skytel.laneguard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<?> handleInvalidRequestException(InvalidRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Error(
                HttpStatus.BAD_REQUEST,
                ex.getReason(),
                System.currentTimeMillis()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflictException(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Error(
                HttpStatus.CONFLICT,
                ex.getReason(),
                System.currentTimeMillis()));
    }

    private record Error(HttpStatus status, String message, long timestamp) {
    }
}
