package dev.crepe.domain.core.util.history.transfer.model;

public enum TransactionType {
    DEPOSIT,        // 입금
    WITHDRAW,       // 출금
    TRANSFER,       // 이체
    INTEREST,       // 이자지급
    SETTLEMENT,     // 정산
    REFUND,         // 환불
}
