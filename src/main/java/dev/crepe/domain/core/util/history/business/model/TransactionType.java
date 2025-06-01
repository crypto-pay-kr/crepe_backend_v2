package dev.crepe.domain.core.util.history.business.model;

public enum TransactionType {
    DEPOSIT,        // 입금
    WITHDRAW,       // 출금
    INTEREST,       // 이자지급
    SETTLEMENT,     // 정산
    REFUND,         // 환불
    PAY,            //결제
    CANCEL,          //주문 취소
    TRANSFER,      // 송금
}
