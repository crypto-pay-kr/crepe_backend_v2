package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InvalidEnumValueException extends LocalizedMessageException {
    public InvalidEnumValueException(String enumType, String invalidValue) {
        super(HttpStatus.BAD_REQUEST, "error.invalid.enum.value", enumType, invalidValue);
    }
}