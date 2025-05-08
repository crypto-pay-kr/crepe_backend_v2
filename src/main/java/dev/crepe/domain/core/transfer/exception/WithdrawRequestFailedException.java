package dev.crepe.domain.core.transfer.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class WithdrawRequestFailedException extends LocalizedMessageException {

    public WithdrawRequestFailedException() {
        super(HttpStatus.BAD_REQUEST, "withdraw.failed");
    }

}