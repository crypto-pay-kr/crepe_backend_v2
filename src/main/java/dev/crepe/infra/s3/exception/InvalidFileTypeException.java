package dev.crepe.infra.s3.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends LocalizedMessageException {
    public InvalidFileTypeException() {
        super(HttpStatus.BAD_REQUEST, "invalid.file.type");
    }
}