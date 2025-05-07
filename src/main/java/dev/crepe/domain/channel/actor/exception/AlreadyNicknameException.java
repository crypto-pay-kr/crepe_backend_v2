package dev.crepe.domain.channel.actor.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyNicknameException extends LocalizedMessageException {
    public AlreadyNicknameException() {
        super(HttpStatus.FORBIDDEN, "already.exist.nickname");
    }
}
