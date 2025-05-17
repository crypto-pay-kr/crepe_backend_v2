package dev.crepe.domain.core.util.coin.regulation.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InvalidTokenGenerateException extends LocalizedMessageException {

    public InvalidTokenGenerateException(String message) {
        super(HttpStatus.BAD_REQUEST, "token.generate.invalid", message);
    }


}
