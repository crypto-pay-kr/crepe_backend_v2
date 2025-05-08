package dev.crepe.domain.core.transfer.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class DuplicateTransactionException extends LocalizedMessageException {
    public DuplicateTransactionException(String txid) {
        super(HttpStatus.CONFLICT, "duplicate.transaction", txid);
    }
}