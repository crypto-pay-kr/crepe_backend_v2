package dev.crepe.domain.core.account.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class MissingAccountRequestException extends LocalizedMessageException {

    public MissingAccountRequestException() {
        super(HttpStatus.BAD_REQUEST, "account.request.missing");
    }
}