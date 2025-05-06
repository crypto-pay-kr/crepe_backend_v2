package dev.crepe.global.error.exception;


import dev.crepe.global.error.CustomHttpStatus;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
public class LocalizedMessageException extends RuntimeException {

    private final int code;
    private final String status;
    private final String messageId;
    private final Object[] arguments;
    private String customMessage = null;

    public LocalizedMessageException(HttpStatus status, String messageId, Object... arguments) {
        super(formatMessage(messageId, arguments));
        this.code = status.value();
        this.status = status.name();
        this.messageId = messageId;
        this.arguments = arguments;
    }

    public LocalizedMessageException(Throwable cause, HttpStatus status, String messageId, Object... arguments) {
        super(formatMessage(messageId, arguments), cause);
        this.code = status.value();
        this.status = status.name();
        this.messageId = messageId;
        this.arguments = arguments;
    }

    public LocalizedMessageException(CustomHttpStatus status, String messageId, Object... arguments) {
        super(formatMessage(messageId, arguments));
        this.code = status.value();
        this.status = status.name();
        this.messageId = messageId;
        this.arguments = arguments;
    }

    protected void setCustomMessage(String message) {
        this.customMessage = message;
    }

    public List<Object> getMessages(MessageSource messageSource, Locale locale) {
        return List.of(Objects.requireNonNullElseGet(customMessage, () -> getDefaultMessage(messageSource, locale)));
    }

    public String getDefaultMessage(MessageSource messageSource, Locale locale) {
        return messageSource.getMessage(messageId, arguments, locale);
    }

    public String getCode() {
        return getClass().getSimpleName();
    }

    public int getStatusCode() {
        return this.code;
    }

    public static LocalizedMessageException of(Exception e) {
        return new UnexpectedException(e);
    }

    private static String formatMessage(String messageId, Object... arguments) {
        if (arguments.length == 0) {
            return messageId;
        }

        StringBuilder sb = new StringBuilder(messageId);
        sb.append(": ");
        for (int i = 0; i < arguments.length; i++) {
            sb.append(arguments[i]);
            if (i != arguments.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
