package dev.crepe.domain.channel.actor.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends LocalizedMessageException {
    public InvalidPasswordException() {
        super(HttpStatus.FORBIDDEN, "invalid.password");
    }
}
