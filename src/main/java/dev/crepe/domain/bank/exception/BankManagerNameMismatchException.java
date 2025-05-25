package dev.crepe.domain.bank.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class BankManagerNameMismatchException extends LocalizedMessageException {

    public BankManagerNameMismatchException(String requestedName, String registeredName) {
        super(HttpStatus.BAD_REQUEST, "bank.manager.name.mismatch", requestedName, registeredName);
    }
}