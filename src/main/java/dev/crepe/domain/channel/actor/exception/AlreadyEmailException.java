package dev.crepe.domain.channel.actor.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyEmailException extends LocalizedMessageException {
    public AlreadyEmailException() {
        super(HttpStatus.FORBIDDEN, "already.exist.email");
}}
