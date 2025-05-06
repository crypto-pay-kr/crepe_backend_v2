package dev.crepe.infra.sms.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class SmsAuthNotFoundException extends LocalizedMessageException {
    public SmsAuthNotFoundException() {
        super(HttpStatus.NOT_FOUND, "notfound.sms-auth");
    }
}
