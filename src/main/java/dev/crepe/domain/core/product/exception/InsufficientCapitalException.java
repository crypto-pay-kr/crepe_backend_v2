package dev.crepe.domain.core.product.exception;

public class InsufficientCapitalException extends RuntimeException {
    public InsufficientCapitalException(String message) {
        super(message);
    }
}
