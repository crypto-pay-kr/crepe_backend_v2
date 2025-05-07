package dev.crepe.domain.channel.actor.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class LoginFailedException extends LocalizedMessageException {
    public LoginFailedException() {
        super(HttpStatus.FORBIDDEN, "failed.login");
    }
}
