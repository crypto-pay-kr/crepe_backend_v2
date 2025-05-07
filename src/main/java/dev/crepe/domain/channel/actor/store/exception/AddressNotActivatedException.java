package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AddressNotActivatedException extends LocalizedMessageException {
    public AddressNotActivatedException(String currency) {
        super(HttpStatus.BAD_REQUEST, "storeaccount.not.activated", currency);
    }
}
