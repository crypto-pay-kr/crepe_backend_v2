package dev.crepe.domain.core.account.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends LocalizedMessageException {
    public InsufficientBalanceException() {
        super(HttpStatus.BAD_REQUEST, "insufficient.balance");
    }
}