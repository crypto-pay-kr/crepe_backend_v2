package dev.crepe.domain.core.transfer.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String requested, String actual, String txid) {
        super("요청한 코인(" + requested + ")과 실제 입금된 코인(" + actual + ")이 일치하지 않습니다. txid=" + txid);
    }
}