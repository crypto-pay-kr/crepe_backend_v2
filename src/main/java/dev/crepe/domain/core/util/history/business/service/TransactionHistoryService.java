package dev.crepe.domain.core.util.history.business.service;

import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import org.springframework.data.domain.Slice;


public interface TransactionHistoryService {

    GetTransactionHistoryResponse getTransactionHistory(TransactionHistory tx);
}
