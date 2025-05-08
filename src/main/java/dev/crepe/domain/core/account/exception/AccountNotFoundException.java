package dev.crepe.domain.core.account.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends LocalizedMessageException {

    public AccountNotFoundException(String email) {
        super(HttpStatus.NOT_FOUND, "account.notfound", email);
    }
    public AccountNotFoundException() {
        super(HttpStatus.NOT_FOUND,"존재하지 않는 계좌입니다.");
    }
}
