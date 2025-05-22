package dev.crepe.domain.core.subscribe.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class NoDepositBalanceException extends LocalizedMessageException {
    public NoDepositBalanceException() {
        super(HttpStatus.BAD_REQUEST, "subscribe.no.balance");
    }
}
