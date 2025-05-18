package dev.crepe.domain.core.exchange.exception;


import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class ExchangeValidationException extends LocalizedMessageException {
  public ExchangeValidationException(String message) {
    super(HttpStatus.BAD_REQUEST,"exchange.invalid.request", message);
  }
}
