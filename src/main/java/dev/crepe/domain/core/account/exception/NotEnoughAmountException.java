package dev.crepe.domain.core.account.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class NotEnoughAmountException extends LocalizedMessageException {

    public NotEnoughAmountException(String message) {
        super(HttpStatus.BAD_REQUEST, "balance.not.enough", message);
    }
}