package dev.crepe.domain.channel.actor.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyPhoneNumberException extends LocalizedMessageException {
    public AlreadyPhoneNumberException() {
        super(HttpStatus.FORBIDDEN, "already.exist.phonenumber");
    }
}
