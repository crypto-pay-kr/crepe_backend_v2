package dev.crepe.domain.core.util.history.transfer.service;

import dev.crepe.domain.core.util.history.transfer.model.dto.GetTransactionHistoryResponse;
import java.util.List;

public interface TransactionHistoryService {

    List<GetTransactionHistoryResponse> getTransactionHistory(String email, String unit);


}
