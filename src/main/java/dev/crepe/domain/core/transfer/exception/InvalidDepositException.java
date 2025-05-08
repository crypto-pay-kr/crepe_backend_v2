package dev.crepe.domain.core.transfer.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InvalidDepositException extends LocalizedMessageException {
    public InvalidDepositException() {
        super(HttpStatus.BAD_REQUEST, "invalid.deposit");
    }
}