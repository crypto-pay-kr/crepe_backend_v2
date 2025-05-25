package dev.crepe.domain.core.util.history.business.service.impl;

import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.history.business.model.dto.CoinUsageDto;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import dev.crepe.domain.core.util.history.exchange.repositroy.ExchangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    @Override
    public GetTransactionHistoryResponse getTransactionHistory(TransactionHistory tx) {
        return GetTransactionHistoryResponse.builder()
                .type(tx.getType().name())
                .status(tx.getStatus().name())
                .amount(tx.getAmount())
                .afterBalance(tx.getAfterBalance())
                .transferredAt(tx.getUpdatedAt())
                .build();
    }

    public BigDecimal getUserCoinTransactionTotal() {
        return transactionHistoryRepository.sumTransactionAmountByUserRole();
    }

    public List<CoinUsageDto> getCoinUsageForUsers() {
        return transactionHistoryRepository.getUsageByCoinFiltered();
    }


}