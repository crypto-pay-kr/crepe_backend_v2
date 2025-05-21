package dev.crepe.domain.core.subscribe.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class UnsupportedProductTypeException extends LocalizedMessageException {
    public UnsupportedProductTypeException() {
        super(HttpStatus.BAD_REQUEST, "product.interest.unsupported");
    }
}
