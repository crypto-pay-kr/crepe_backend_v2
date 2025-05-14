package dev.crepe.domain.core.pay.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class StoreAlreadySettledException extends LocalizedMessageException {
    public StoreAlreadySettledException() {
        super(HttpStatus.BAD_REQUEST, "store.already.settled");
    }
}
