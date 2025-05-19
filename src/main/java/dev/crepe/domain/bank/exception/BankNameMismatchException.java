package dev.crepe.domain.bank.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class BankNameMismatchException extends LocalizedMessageException {

    public BankNameMismatchException(String requestedName, String registeredName) {
        super(HttpStatus.BAD_REQUEST, "bank.name.mismatch", requestedName, registeredName);
    }
}