package dev.crepe.domain.core.util.coin.regulation.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class BankTokenNotFoundException extends LocalizedMessageException {

    public BankTokenNotFoundException(String bankName) {
        super(HttpStatus.NOT_FOUND, "bank.token.not.found", bankName);
    }
}