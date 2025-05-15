package dev.crepe.domain.core.util.history.business.service.impl;

import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AccountRepository accountRepository;

    public Slice<GetTransactionHistoryResponse> getTransactionHistory (String email, String currency, int page, int size) {

        Account account = accountRepository.findByActor_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(email));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return transactionHistoryRepository
                .findByAccount_Id(account.getId(), pageable)
                .map(history -> GetTransactionHistoryResponse.builder()
                        .status(history.getStatus().name())
                        .type(history.getType().name())
                        .amount(history.getAmount())
                        .afterBalance(history.getAfterBalance())
                        .transferredAt(history.getUpdatedAt())
                        .build());

    }
}