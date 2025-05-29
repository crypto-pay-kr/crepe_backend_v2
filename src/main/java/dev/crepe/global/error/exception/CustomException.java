package dev.crepe.global.error.exception;

import dev.crepe.global.error.exception.model.ExceptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{

    private final String code;
    private final ExceptionStatus status;

    public CustomException(String code, ExceptionStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
