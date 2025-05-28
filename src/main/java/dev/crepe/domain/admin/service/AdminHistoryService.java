package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import dev.crepe.domain.admin.dto.response.GetSettlementHistoryResponse;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminHistoryService {

    Page<GetPayHistoryResponse> getPayHistoriesByUserId(Long userId, String type, Pageable pageable);
    Page<GetSettlementHistoryResponse> getSettlementHistoriesByUserId(Long storeId, TransactionStatus status, Pageable pageable);
    void reSettlement(Long historyId);
}
