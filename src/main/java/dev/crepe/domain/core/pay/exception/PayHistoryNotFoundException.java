package dev.crepe.domain.core.pay.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.hibernate.sql.results.spi.LoadContexts;
import org.springframework.http.HttpStatus;

public class PayHistoryNotFoundException extends LocalizedMessageException {
    public PayHistoryNotFoundException() {
        super(HttpStatus.NOT_FOUND, "payHistroy.not.found");
    }
}