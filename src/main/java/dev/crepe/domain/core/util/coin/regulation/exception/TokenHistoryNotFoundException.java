package dev.crepe.domain.core.util.coin.regulation.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class TokenHistoryNotFoundException extends LocalizedMessageException {

    public TokenHistoryNotFoundException(Long tokenHistoryId) {
        super(HttpStatus.NOT_FOUND, "token.history.not.found", tokenHistoryId);
    }
}