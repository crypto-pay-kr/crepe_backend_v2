package dev.crepe.infra.redis.util;

import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionCreatedEvent {
    private Long accountId;
    private String userEmail;
    private String currency;
    private String transactionType;
    private GetTransactionHistoryResponse transactionResponse; // 최근 거래 캐시용
}