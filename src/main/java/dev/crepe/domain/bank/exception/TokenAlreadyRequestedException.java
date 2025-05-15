package dev.crepe.domain.bank.exception;

public class TokenAlreadyRequestedException extends RuntimeException {
    public TokenAlreadyRequestedException(String message) {
        super(message);
    }
}
