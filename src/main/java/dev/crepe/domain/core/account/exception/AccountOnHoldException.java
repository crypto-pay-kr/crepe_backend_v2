package dev.crepe.domain.core.account.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AccountOnHoldException extends LocalizedMessageException {

    public AccountOnHoldException() {
        super(HttpStatus.BAD_REQUEST, "account.on.hold");
    }
}