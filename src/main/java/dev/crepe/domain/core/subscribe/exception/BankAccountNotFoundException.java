package dev.crepe.domain.core.subscribe.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class BankAccountNotFoundException extends LocalizedMessageException {
    public BankAccountNotFoundException() {
        super(HttpStatus.NOT_FOUND, "account.bank.notfound");
    }
}
