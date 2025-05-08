package dev.crepe.domain.core.account.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class TagRequiredException extends LocalizedMessageException {
    public TagRequiredException(String currency) {
        super(HttpStatus.BAD_REQUEST, "coin.tag.required", currency);
    }
}