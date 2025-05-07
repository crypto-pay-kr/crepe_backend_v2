package dev.crepe.global.error.exception;

import org.springframework.http.HttpStatus;

public class InvalidEnumValueException extends LocalizedMessageException {
    public InvalidEnumValueException(String enumType, String invalidValue) {
        super(HttpStatus.BAD_REQUEST, "error.invalid.enum.value", enumType, invalidValue);
    }
}