package dev.crepe.domain.core.util.coin.regulation.exception;

public class TokenAlreadyRequestedException extends RuntimeException {
    public TokenAlreadyRequestedException(String message) {
        super(message);
    }
}
