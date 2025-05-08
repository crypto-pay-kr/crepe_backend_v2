package dev.crepe.domain.core.transfer.exception;


public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String txid) {
        super("이미 처리된 거래입니다. txid=" + txid);
    }
}