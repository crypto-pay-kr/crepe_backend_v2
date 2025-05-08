package dev.crepe.domain.core.util.history.transfer.service.impl;

import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.history.transfer.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.transfer.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.transfer.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AccountRepository accountRepository;

    public List<GetTransactionHistoryResponse> getTransactionHistory (String email, String currency) {

        Account account = accountRepository.findByActor_EmailAndCoin_Currency(email, currency)
                .orElseThrow(() -> new AccountNotFoundException(email));

        return transactionHistoryRepository.findByAccount_IdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(history -> GetTransactionHistoryResponse.builder()
                        .status(history.getStatus().name())
                        .type(history.getType().name())
                        .amount(history.getAmount())
                        .transferredAt(history.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

    }
}