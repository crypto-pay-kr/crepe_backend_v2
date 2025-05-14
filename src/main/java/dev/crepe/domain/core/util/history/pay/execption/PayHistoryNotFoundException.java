package dev.crepe.domain.core.util.history.pay.execption;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class PayHistoryNotFoundException extends LocalizedMessageException {
    public PayHistoryNotFoundException() {
        super(HttpStatus.NOT_FOUND, "payHistroy.not.found");
    }
}