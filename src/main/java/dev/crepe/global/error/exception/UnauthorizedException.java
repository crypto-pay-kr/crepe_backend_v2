package dev.crepe.global.error.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends LocalizedMessageException {
    public UnauthorizedException(String message) {
        super(HttpStatus.FORBIDDEN, "invalid.authorization");
    }
}
