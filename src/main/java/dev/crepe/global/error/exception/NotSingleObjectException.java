package dev.crepe.global.error.exception;

import org.springframework.http.HttpStatus;

public class NotSingleObjectException extends LocalizedMessageException {
    public NotSingleObjectException() {
        super(HttpStatus.CONFLICT, "error.db.multiple.results.found");
    }
}
