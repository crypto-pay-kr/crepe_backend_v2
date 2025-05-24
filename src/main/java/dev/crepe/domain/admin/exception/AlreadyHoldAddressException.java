package dev.crepe.domain.admin.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyHoldAddressException extends LocalizedMessageException {

    public AlreadyHoldAddressException(String address) {
        super(HttpStatus.BAD_REQUEST, "address.already.hold", address);
    }
}