package dev.crepe.domain.channel.actor.user.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends LocalizedMessageException {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "notfound.user");
    }
}
