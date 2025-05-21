package dev.crepe.domain.core.subscribe.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class UserAccountNotFoundException extends LocalizedMessageException {
    public UserAccountNotFoundException() {
        super(HttpStatus.NOT_FOUND, "account.user.notfound");
    }
}