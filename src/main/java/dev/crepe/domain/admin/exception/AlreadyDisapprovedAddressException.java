package dev.crepe.domain.admin.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyDisapprovedAddressException extends LocalizedMessageException {

    public AlreadyDisapprovedAddressException(String address) {
        super(HttpStatus.BAD_REQUEST, "address.already.disapproved", address);
    }
}