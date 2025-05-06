package dev.crepe.global.error.exception;

import org.springframework.http.HttpStatus;

public class UnexpectedException extends LocalizedMessageException {
    public UnexpectedException(Throwable e) {
        super(e, HttpStatus.INTERNAL_SERVER_ERROR, "unexpected");
    }

    @Override
    public String getCode() {
        return getCause().getClass().getSimpleName();
    }
}
