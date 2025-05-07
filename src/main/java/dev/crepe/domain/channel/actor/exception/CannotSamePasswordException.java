package dev.crepe.domain.channel.actor.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class CannotSamePasswordException extends LocalizedMessageException {
    public CannotSamePasswordException() {
        super(HttpStatus.FORBIDDEN, "failed.change.password");
    }
}
