package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class EmptyValueException extends LocalizedMessageException {
    public EmptyValueException(String fieldName) {
        super(HttpStatus.BAD_REQUEST, "empty.value.error", fieldName);
    }
}