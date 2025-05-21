package dev.crepe.domain.core.deposit.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class ExceedMonthlyLimitException extends LocalizedMessageException {
    public ExceedMonthlyLimitException() {
        super(HttpStatus.BAD_REQUEST, "deposit.exceed.monthly.limit");
    }
}