package dev.crepe.infra.sms.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class SmsAuthCodeNotValidException extends LocalizedMessageException {
    public SmsAuthCodeNotValidException() {
        super(HttpStatus.BAD_REQUEST, "invalid.sms-code");
    }
}
