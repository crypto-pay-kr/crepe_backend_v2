package dev.crepe.domain.core.util.coin.regulation.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class TokenGenerateFailedException extends RuntimeException {

    public TokenGenerateFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}