package dev.crepe.infra.sms.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class SmsException extends LocalizedMessageException {

    public SmsException() {
        super(HttpStatus.BAD_REQUEST, "invalid.sms");
    }

    public SmsException(HttpStatus httpStatus, String messageTag) {
        super(httpStatus, messageTag);
    }
}
