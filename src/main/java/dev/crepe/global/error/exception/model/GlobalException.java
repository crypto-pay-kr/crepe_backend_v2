package dev.crepe.global.error.exception.model;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final String code;

    public GlobalException(String code) {
        super(code);
        this.code = code;
    }

    public GlobalException(String code, String message) {
        super(message);
        this.code = code;
    }
}
