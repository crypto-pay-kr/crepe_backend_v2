package dev.crepe.domain.admin.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyRejectedAddressException extends LocalizedMessageException {

    public AlreadyRejectedAddressException(String address) {
        super(HttpStatus.BAD_REQUEST, "address.already.reject", address);
    }
}