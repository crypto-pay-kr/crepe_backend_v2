package dev.crepe.global.error.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends LocalizedMessageException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "not_found.resource");
    }
}
