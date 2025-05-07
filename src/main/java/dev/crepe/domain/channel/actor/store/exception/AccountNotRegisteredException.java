package dev.crepe.domain.channel.actor.store.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AccountNotRegisteredException extends LocalizedMessageException {

    public AccountNotRegisteredException() {
        super(HttpStatus.BAD_REQUEST, "account.not.registered");
    }
}