package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class UnauthorizedStoreAccessException extends LocalizedMessageException {
    public UnauthorizedStoreAccessException(String email) {
        super(HttpStatus.FORBIDDEN, "unauthorized.store.access", email);
    }
}