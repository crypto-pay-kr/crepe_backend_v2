package dev.crepe.infra.sms.exception;

import org.springframework.http.HttpStatus;

public class CannotSendSmsException extends SmsException {
    public CannotSendSmsException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "error.nhn.server");
    }
}
