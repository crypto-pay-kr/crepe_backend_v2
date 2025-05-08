package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class StoreNotFoundException extends LocalizedMessageException {
    public StoreNotFoundException(Long storeId) {
        super(HttpStatus.NOT_FOUND, "store.not.found", storeId);
    }
}