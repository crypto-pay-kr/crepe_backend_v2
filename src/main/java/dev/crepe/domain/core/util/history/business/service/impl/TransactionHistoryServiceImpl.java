package dev.crepe.domain.core.util.history.business.service.impl;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.dto.CoinUsageDto;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.model.dto.PayMonthlyAmountDto;
import dev.crepe.domain.core.util.history.business.model.dto.PayStatusCountDto;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AccountRepository accountRepository;

    @Override
    public GetTransactionHistoryResponse getTransactionHistory(TransactionHistory tx) {
        return GetTransactionHistoryResponse.builder()
                .type(tx.getType().name())
                .status(tx.getStatus().name())
                .name(tx.getTransactionId())
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


    public List<PayMonthlyAmountDto> getMonthlyPayAmount(String email){
        return transactionHistoryRepository.findMonthlyAcceptedTransactionTotalsByEmail(email);
    }


    public List<PayStatusCountDto>  getPayStatusCount(String email){
        return transactionHistoryRepository.countTotalByStatus(email);
    }

    @Override
    public Page<TransactionHistory> getSettlementHistory(TransactionStatus status, Long storeId, Pageable pageable) {
        if (status == null) {
            return transactionHistoryRepository.findByTypeAndAccount_Actor_Id(TransactionType.PAY, storeId, pageable);
        }
        return transactionHistoryRepository.findByTypeAndStatusAndAccount_Actor_Id(TransactionType.PAY,status,storeId,pageable);
    }

    @Override
    public void reSettlement(Long historyId) {
        // 1. 기존 거래 조회
        TransactionHistory history = transactionHistoryRepository.getTransactionHistoryById(historyId);

        // 2. 금액 및 계좌
        Account account = history.getAccount();
        BigDecimal amount = history.getAmount();

        // 3. 현재 잔액 계산 (계좌에 추가)
        BigDecimal newBalance = account.getBalance().add(amount);
        account.addAmount(amount);
        accountRepository.save(account);

        // 4. 재정산용 내역 생성
        TransactionHistory reSettlementHistory = TransactionHistory.builder()
                .account(account)
                .amount(amount)
                .type(TransactionType.PAY)
                .status(TransactionStatus.ACCEPTED)
                .afterBalance(newBalance)
                .build();

        transactionHistoryRepository.save(reSettlementHistory);
    }

}