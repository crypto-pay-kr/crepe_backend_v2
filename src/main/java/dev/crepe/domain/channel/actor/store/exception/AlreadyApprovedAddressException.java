package dev.crepe.domain.channel.actor.store.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyApprovedAddressException extends LocalizedMessageException {

    public AlreadyApprovedAddressException(String address) {
        super(HttpStatus.BAD_REQUEST, "address.already.approved", address);
    }
}