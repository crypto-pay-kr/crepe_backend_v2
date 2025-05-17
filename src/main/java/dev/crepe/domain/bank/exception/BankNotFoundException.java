package dev.crepe.domain.bank.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class BankNotFoundException extends LocalizedMessageException {

    public BankNotFoundException(Long bankId) {
        super(HttpStatus.NOT_FOUND, "bank.not.found", bankId);
    }

    public BankNotFoundException(String bankEmail) {
        super(HttpStatus.NOT_FOUND, "bank.not.found", bankEmail);
    }
}