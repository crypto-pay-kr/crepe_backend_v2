package dev.crepe.domain.core.deposit.exception;

import dev.crepe.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyDepositedException extends LocalizedMessageException {
  public AlreadyDepositedException() {
    super(HttpStatus.BAD_REQUEST, "deposit.already.once");
  }
}