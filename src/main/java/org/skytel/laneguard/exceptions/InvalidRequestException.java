package org.skytel.laneguard.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidRequestException extends ResponseStatusException {
    public InvalidRequestException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }
}
