package dev.crepe.domain.core.account.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class NotBankAccountException extends LocalizedMessageException {

    public NotBankAccountException() {
        super(HttpStatus.BAD_REQUEST, "account.owner.not.bank");
    }
}