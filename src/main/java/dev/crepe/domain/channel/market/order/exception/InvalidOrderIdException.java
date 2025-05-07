package dev.crepe.domain.channel.market.order.exception;

public class InvalidOrderIdException extends RuntimeException {

    public InvalidOrderIdException(String message) {
        super(message);
    }

    public InvalidOrderIdException(String message, Throwable cause) {
        super(message, cause);
    }
}