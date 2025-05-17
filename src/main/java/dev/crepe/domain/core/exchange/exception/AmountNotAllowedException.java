package dev.crepe.domain.core.exchange.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AmountNotAllowedException extends LocalizedMessageException {
    public AmountNotAllowedException() {
        super(HttpStatus.BAD_REQUEST,"amount.not.allowed");
    }
}
