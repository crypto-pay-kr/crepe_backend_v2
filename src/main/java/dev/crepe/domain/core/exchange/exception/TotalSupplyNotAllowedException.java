package dev.crepe.domain.core.exchange.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class TotalSupplyNotAllowedException extends LocalizedMessageException {
    public TotalSupplyNotAllowedException() {
        super(HttpStatus.BAD_REQUEST,"total.token.not.allowed");
    }
}
