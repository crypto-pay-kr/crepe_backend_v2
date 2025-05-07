package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class StoreAccountNotFoundException extends LocalizedMessageException {

    public StoreAccountNotFoundException(String email) {
        super(HttpStatus.NOT_FOUND, "storeaccount.notfound", email);
    }
    public StoreAccountNotFoundException() {
        super(HttpStatus.NOT_FOUND,"존재하지 않는 계좌입니다.");
    }
}
