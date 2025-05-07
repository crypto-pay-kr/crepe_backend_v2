package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class StoreNotFoundException extends LocalizedMessageException {
    public StoreNotFoundException(String email) {
        super(HttpStatus.NOT_FOUND, "error.store.not.found", email);
    }
}