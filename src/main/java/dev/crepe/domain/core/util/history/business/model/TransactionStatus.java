package dev.crepe.domain.core.util.history.business.model;

public enum TransactionStatus {
    PENDING,  // 대기
    ACCEPTED,  // 성공
    FAILED,    // 실패
    REFUNDED,
}
