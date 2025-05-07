package dev.crepe.domain.channel.actor.store.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InvalidActionException extends LocalizedMessageException {
    public InvalidActionException(String action) {
        super(HttpStatus.BAD_REQUEST, "error.invalid.action", action);
    }
}