package dev.crepe.domain.core.account.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class NotActorAccountOwnerException extends LocalizedMessageException {

    public NotActorAccountOwnerException() {
        super(HttpStatus.BAD_REQUEST, "account.owner.not.actor");
    }
}