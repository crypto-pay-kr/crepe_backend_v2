package dev.crepe.domain.core.util.coin.regulation.exception;
import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InvalidPortfolioException extends LocalizedMessageException {

    public InvalidPortfolioException(String messageKey, Object... args) {
        super(HttpStatus.BAD_REQUEST, messageKey, args);
    }
}