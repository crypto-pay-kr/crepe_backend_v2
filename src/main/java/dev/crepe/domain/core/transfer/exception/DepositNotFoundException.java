package dev.crepe.domain.core.transfer.exception;

public class DepositNotFoundException extends RuntimeException {
    public DepositNotFoundException(String txid) {
        super("해당 txid에 대한 입금 정보를 찾을 수 없습니다. txid=" + txid);
    }
}