package dev.crepe.domain.core.transfer.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class DepositRequestFailedException extends LocalizedMessageException {
    public DepositRequestFailedException(String txid) {
        super(HttpStatus.NOT_FOUND, "deposit.failed", txid);
    }
}