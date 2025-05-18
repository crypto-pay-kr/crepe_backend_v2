package dev.crepe.domain.core.util.coin.regulation.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class PendingBankTokenExistsException extends LocalizedMessageException {

    public PendingBankTokenExistsException(String tokenName) {
        super(HttpStatus.BAD_REQUEST, "bank.token.pending.exists", tokenName);
    }
}