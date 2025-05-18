package dev.crepe.domain.core.util.coin.regulation.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InvalidBankTokenStatusException extends LocalizedMessageException {

    public InvalidBankTokenStatusException(String status) {
        super(HttpStatus.BAD_REQUEST, "bank.token.status.invalid", status);
    }
}