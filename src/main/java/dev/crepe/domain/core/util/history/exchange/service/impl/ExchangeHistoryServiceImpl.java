package dev.crepe.domain.core.util.history.exchange.service.impl;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import dev.crepe.domain.core.util.history.exchange.repositroy.ExchangeHistoryRepository;
import dev.crepe.domain.core.util.history.exchange.service.ExchangeHistoryService;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeHistoryServiceImpl implements ExchangeHistoryService {

    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final AccountRepository accountRepository;
    private final SubscribeHistoryRepository subscribeHistoryRepository;
    private final TransactionHistoryService txhService;
    private final TransactionHistoryRepository txhRepo;
    private final ExceptionDbService exceptionDbService;
    @Override
    public GetTransactionHistoryResponse getExchangeHistory(ExchangeHistory ex, Account userAccount) {
        boolean isSender = ex.getFromAccount().getId().equals(userAccount.getId());

        BigDecimal rawAmount = isSender ? ex.getFromAmount() : ex.getToAmount();
        BigDecimal signedAmount = isSender ? rawAmount.negate() : rawAmount;

        return GetTransactionHistoryResponse.builder()
                .type("EXCHANGE")
                .status("ACCEPTED")
                .amount(signedAmount)
                .afterBalance(isSender ? ex.getAfterCoinBalanceFrom() : ex.getAfterCoinBalanceTo())
                .transferredAt(ex.getCreatedAt())
                .build();
    }



    @Override
    public Slice<GetTransactionHistoryResponse> getExchangeHistoryList(String email, String currency, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Account> userAccounts = accountRepository.findByActor_Email(email);

        if (userAccounts.isEmpty()) {
            return new SliceImpl<>(List.of(), pageRequest, false);
        }

        List<Long> userAccountIds = userAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        List<GetTransactionHistoryResponse> resultList = new ArrayList<>();

        List<ExchangeHistory> allExchanges = exchangeHistoryRepository.findByFromAccount_IdInOrToAccount_IdIn(
                userAccountIds, userAccountIds);

        for (ExchangeHistory ex : allExchanges) {

            Account fromAccount = ex.getFromAccount();
            Account toAccount = ex.getToAccount();

            boolean isFromAccountMine = userAccountIds.contains(fromAccount.getId());

            boolean isToAccountMine = userAccountIds.contains(toAccount.getId());

            boolean isRelatedToCurrency = false;
            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal afterBalance = BigDecimal.ZERO;


            if (isFromAccountMine && toAccount.getBankToken() != null &&
                    toAccount.getBankToken().getCurrency().equalsIgnoreCase(currency)) {
                isRelatedToCurrency = true;
                amount = ex.getToAmount();
                afterBalance = ex.getAfterTokenBalanceFrom();
            }
            else if (isToAccountMine && fromAccount.getBankToken() != null &&
                    fromAccount.getBankToken().getCurrency().equalsIgnoreCase(currency)) {
                isRelatedToCurrency = true;

                amount = ex.getFromAmount().negate();
                afterBalance = ex.getAfterTokenBalanceTo();
            }

            if (!isRelatedToCurrency) {
                continue;
            }
            resultList.add(
                    GetTransactionHistoryResponse.builder()
                            .type("EXCHANGE")
                            .status("ACCEPTED")
                            .amount(amount)
                            .afterBalance(afterBalance)
                            .transferredAt(ex.getCreatedAt())
                            .build()
            );
        }

        // 2. 구독 거래 내역 포함
        List<SubscribeHistory> subscribeHistories = subscribeHistoryRepository.findBySubscribe_User_Email(email);

        for (SubscribeHistory sh : subscribeHistories) {
            Subscribe subscribe = sh.getSubscribe();

            if (!subscribe.getProduct().getBankToken().getCurrency().equalsIgnoreCase(currency)) continue;
            if ("PAYMENT".equalsIgnoreCase(sh.getEventType().name())) continue;
            Account account = accountRepository.findByActor_EmailAndBankToken_Currency(email,currency)
                    .orElseThrow(()-> exceptionDbService.getException("ACCOUNT_001"));
            BigDecimal amount = sh.getEventType()== SubscribeHistoryType.TERMINATION
                    ? sh.getAmount()
                    : sh.getAmount().negate();

            resultList.add(GetTransactionHistoryResponse.builder()
                    .type("SUBSCRIBE")
                    .status("ACCEPTED")
                    .amount(amount)
                    .afterBalance(account.getBalance())
                    .transferredAt(sh.getCreatedAt())
                    .build());
        }


        List<Account> tokenAccounts = userAccounts.stream()
                .filter(acc -> acc.getBankToken() != null &&
                        acc.getBankToken().getCurrency().equalsIgnoreCase(currency))
                .toList();

        tokenAccounts.forEach(acc -> {
            List<TransactionHistory> txList = txhRepo.findByAccount_Id(acc.getId());
            txList.stream()
                    .filter(tx -> tx.getType() == TransactionType.TRANSFER)
                    .map(txhService::getTransactionHistory)
                    .forEach(resultList::add);
        });


        tokenAccounts.forEach(acc -> {
            List<TransactionHistory> txList = txhRepo.findByAccount_Id(acc.getId());
            txList.stream()
                    .filter(tx ->
                            tx.getType() == TransactionType.PAY
                    )
                    .map(txhService::getTransactionHistory)
                    .forEach(resultList::add);
        });


        resultList.sort(Comparator.comparing(GetTransactionHistoryResponse::getTransferredAt).reversed());

        int start = page * size;
        int end = Math.min(start + size, resultList.size());

        if (start >= resultList.size()) {
            return new SliceImpl<>(List.of(), pageRequest, false);
        }
        List<GetTransactionHistoryResponse> pageContent = resultList.subList(start, end);
        boolean hasNext = resultList.size() > end;

        return new SliceImpl<>(pageContent, pageRequest, hasNext);
    }
}
