package dev.crepe.domain.core.util.history.business.service;

import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import org.springframework.data.domain.Slice;


public interface TransactionHistoryService {

    Slice<GetTransactionHistoryResponse> getTransactionHistory(String email, String currency, int page, int size);


}
