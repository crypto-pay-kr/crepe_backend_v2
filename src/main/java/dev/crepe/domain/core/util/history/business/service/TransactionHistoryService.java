package dev.crepe.domain.core.util.history.business.service;

import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.dto.CoinUsageDto;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.model.dto.PayMonthlyAmountDto;
import dev.crepe.domain.core.util.history.business.model.dto.PayStatusCountDto;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.math.BigDecimal;
import java.util.List;


public interface TransactionHistoryService {


    GetTransactionHistoryResponse getTransactionHistory(TransactionHistory tx);
    Page<TransactionHistory> getSettlementHistory(TransactionStatus status , Long storeId, Pageable pageable);
    BigDecimal getUserCoinTransactionTotal();
    List<CoinUsageDto> getCoinUsageForUsers();

    List<PayMonthlyAmountDto> getMonthlyPayAmount(String email);
    List<PayStatusCountDto> getPayStatusCount(String email);

    void reSettlement(Long historyId);
}
