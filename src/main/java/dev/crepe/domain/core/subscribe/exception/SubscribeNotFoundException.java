package dev.crepe.domain.core.subscribe.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class SubscribeNotFoundException extends LocalizedMessageException {
    public SubscribeNotFoundException() {
        super(HttpStatus.NOT_FOUND, "subscribe.notfound");
    }
}
