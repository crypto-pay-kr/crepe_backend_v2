package dev.crepe.domain.core.pay.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;

public class AlreadyRefundException extends LocalizedMessageException {
  public AlreadyRefundException() {
    super(HttpStatus.BAD_REQUEST, "Already.Refund");
  }
}
