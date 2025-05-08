package dev.crepe.domain.channel.market.order.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class InvalidOrderIdException extends LocalizedMessageException {

    public InvalidOrderIdException() {
        super(HttpStatus.BAD_REQUEST, "invalid.order.id");
    }
}