package dev.crepe.domain.channel.market.order.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class ExchangePriceNotMatchException extends LocalizedMessageException {
    public ExchangePriceNotMatchException(String currency) {
        super(HttpStatus.BAD_REQUEST, "mismatch.exchange.price", currency);
    }
}