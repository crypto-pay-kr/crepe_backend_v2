package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class CoinNotFoundException extends LocalizedMessageException {
    public CoinNotFoundException(String currency) {
        super(HttpStatus.NOT_FOUND, "coin.notfound", currency);
    }
}