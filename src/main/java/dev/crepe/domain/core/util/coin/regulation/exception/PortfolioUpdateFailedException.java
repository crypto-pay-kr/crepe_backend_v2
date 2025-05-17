package dev.crepe.domain.core.util.coin.regulation.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class PortfolioUpdateFailedException extends LocalizedMessageException {

    public PortfolioUpdateFailedException(String reason) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "portfolio.update.failed", reason);
    }
}