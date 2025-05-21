package dev.crepe.domain.core.subscribe.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class MissingMonthlyDepositException extends LocalizedMessageException {
    public MissingMonthlyDepositException() {
        super(HttpStatus.BAD_REQUEST, "subscribe.missing.deposit");
    }
}
