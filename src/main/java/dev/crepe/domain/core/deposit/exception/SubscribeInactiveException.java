package dev.crepe.domain.core.deposit.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class SubscribeInactiveException extends LocalizedMessageException {
    public SubscribeInactiveException(String status) {
        super(HttpStatus.BAD_REQUEST, "deposit.inactive.status", new Object[]{status});
    }
}