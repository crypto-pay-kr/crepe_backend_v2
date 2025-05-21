package dev.crepe.domain.core.util.history.exchange.service;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import org.springframework.data.domain.Slice;

public interface ExchangeHistoryService {

    GetTransactionHistoryResponse getExchangeHistory(ExchangeHistory ex, Account userAccount);

    Slice<GetTransactionHistoryResponse> getExchangeHistoryList(
            String email, String currency, int page, int size);
}
