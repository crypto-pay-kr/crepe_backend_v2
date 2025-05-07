package dev.crepe.domain.channel.market.order.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends LocalizedMessageException {
    public OrderNotFoundException(String orderId) {
        super(HttpStatus.NOT_FOUND, "notfound.order", orderId);
    }
}