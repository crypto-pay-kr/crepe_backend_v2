package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.dto.response.GetAccountInfoResponse;
import dev.crepe.domain.admin.dto.response.GetAllTransactionHistoryResponse;
import dev.crepe.domain.admin.service.AdminAccountService;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import dev.crepe.domain.core.util.history.exchange.repositroy.ExchangeHistoryRepository;
import dev.crepe.domain.core.util.history.exchange.service.ExchangeHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAccountServiceImpl implements AdminAccountService {

    private final AccountRepository accountRepository;
    private final TransactionHistoryRepository txhRepo;
    private final ExchangeHistoryRepository exhRepo;
    private final TransactionHistoryService txhService;
    private final ExchangeHistoryService exhService;

    @Override
    public Page<GetAccountInfoResponse> getAccountInfo(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accounts = accountRepository.findByActor_Id(id, pageable);

        return accounts.map(account -> {
            String name = account.getCoin() != null
                    ? account.getCoin().getName()
                    : account.getBankToken().getName();

            String currency = account.getCoin() != null
                    ? account.getCoin().getCurrency()
                    : account.getBankToken().getCurrency();

            return GetAccountInfoResponse.builder()
                    .coinName(name)
                    .currency(currency)
                    .address(account.getAccountAddress())
                    .tag(account.getTag())
                    .balance(account.getBalance().toString())
                    .registryStatus(account.getAddressRegistryStatus())
                    .build();
        });
    }


    @Override
    public Page<GetAllTransactionHistoryResponse> getUserFullTransactionHistory(Long actorId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        // 1. 해당 유저의 모든 계좌 가져오기
        List<Account> userAccounts = accountRepository.findByActor_Id(actorId);
        if (userAccounts.isEmpty()) {
            return Page.empty(pageRequest);
        }

        // 2. 계좌 ID 목록
        List<Long> accountIds = userAccounts.stream()
                .map(Account::getId)
                .toList();

        List<GetAllTransactionHistoryResponse> resultList = new ArrayList<>();

        // 3. 일반 거래 이력
        for (Account acc : userAccounts) {
            if (acc.getCoin() != null) {
                List<TransactionHistory> txList = txhRepo.findByAccount_Id(acc.getId());
                resultList.addAll(txList.stream()
                        .map(tx -> GetAllTransactionHistoryResponse.builder()
                                .currency(acc.getCoin().getCurrency())
                                .TransferAt(tx.getCreatedAt().toString())
                                .TransactionId(tx.getTransactionId())
                                .TransactionStatus(tx.getStatus().name())
                                .TransactionType(tx.getType().name())
                                .Amount(tx.getAmount().toPlainString())
                                .build())
                        .toList());
            }
        }

        if (!accountIds.isEmpty()) {
            List<ExchangeHistory> exList = exhRepo.findByFromAccount_IdInOrToAccount_IdIn(accountIds, accountIds);
            resultList.addAll(exList.stream()
                    .map(e -> {
                        Account matchedAccount = userAccounts.stream()
                                .filter(acc -> acc.getId().equals(e.getFromAccount().getId()) ||
                                        acc.getId().equals(e.getToAccount().getId()))
                                .findFirst()
                                .orElse(null);

                        boolean isFrom = matchedAccount != null && matchedAccount.getId().equals(e.getFromAccount().getId());
                        BigDecimal amount = isFrom ? e.getFromAmount() : e.getToAmount();

                        // 금액에 부호 붙이기
                        String amountText = (isFrom ? "-" : "") + amount.toPlainString();

                        String currency = matchedAccount != null && matchedAccount.getCoin() != null
                                ? matchedAccount.getCoin().getCurrency()
                                : matchedAccount != null && matchedAccount.getBankToken() != null
                                ? matchedAccount.getBankToken().getName()
                                : "UNKNOWN";

                        return GetAllTransactionHistoryResponse.builder()
                                .currency(currency)
                                .TransferAt(e.getCreatedAt().toString())
                                .TransactionStatus("ACCEPTED")
                                .TransactionType("EXCHANGE")
                                .Amount(amountText)
                                .build();
                    })
                    .toList());
        }

        // 5. 최신순 정렬
        resultList.sort(Comparator.comparing(GetAllTransactionHistoryResponse::getTransferAt).reversed());

        // 6. Page 처리
        int start = page * size;
        int end = Math.min(start + size, resultList.size());

        if (start >= resultList.size()) {
            return Page.empty(pageRequest);
        }

        List<GetAllTransactionHistoryResponse> pageContent = resultList.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, resultList.size());
    }



}
