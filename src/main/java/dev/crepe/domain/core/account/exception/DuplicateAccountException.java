package dev.crepe.domain.core.account.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class DuplicateAccountException extends LocalizedMessageException {
    public DuplicateAccountException(String currency) {
        super(HttpStatus.BAD_REQUEST, "account.duplicate", currency);
    }
}
