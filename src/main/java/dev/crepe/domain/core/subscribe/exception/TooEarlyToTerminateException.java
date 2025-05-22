package dev.crepe.domain.core.subscribe.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class TooEarlyToTerminateException extends LocalizedMessageException {
    public TooEarlyToTerminateException() {
        super(HttpStatus.FORBIDDEN, "subscribe.terminate.tooearly");
    }
}
