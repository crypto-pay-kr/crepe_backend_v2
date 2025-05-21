package dev.crepe.domain.core.subscribe.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyExpiredSubscribeException extends LocalizedMessageException {
    public AlreadyExpiredSubscribeException() {
        super(HttpStatus.BAD_REQUEST, "subscribe.already.expired");
    }
}
