package dev.crepe.domain.core.util.history.global.service.impl;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import dev.crepe.domain.core.util.history.exchange.repositroy.ExchangeHistoryRepository;
import dev.crepe.domain.core.util.history.exchange.service.ExchangeHistoryService;
import dev.crepe.domain.core.util.history.global.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class HistoryServiceImpl implements HistoryService {

    private final TransactionHistoryRepository txhRepo;
    private final ExchangeHistoryRepository exhRepo;
    private final AccountRepository accountRepository;
    private final TransactionHistoryService txhService;
    private final ExchangeHistoryService exhService;

    @Override
    public Slice<GetTransactionHistoryResponse> getNonRegulationHistory(String email, String currency, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        // 1. 사용자의 전체 계좌 조회
        List<Account> userAccounts = accountRepository.findByActor_Email(email);

        if (userAccounts.isEmpty()) {
            return new SliceImpl<>(List.of(), pageRequest, false);
        }

        // 2. 현재 요청된 화폐(currency)에 해당하는 계좌만 필터링
        List<Account> relevantAccounts = userAccounts.stream()
                .filter(acc -> {
                    if (acc.getCoin() != null) {
                        return acc.getCoin().getCurrency().equalsIgnoreCase(currency);
                    }
                    return false;
                })
                .collect(Collectors.toList());

        List<Long> relevantAccountIds = relevantAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        List<GetTransactionHistoryResponse> resultList = new ArrayList<>();

        // 3. 일반 거래 이력 (코인 계좌)
        relevantAccounts.stream()
                .filter(acc -> acc.getCoin() != null) // 코인 계좌인 경우
                .forEach(acc -> {
                    List<TransactionHistory> txList = txhRepo.findByAccount_Id(acc.getId());
                    resultList.addAll(txList.stream().map(txhService::getTransactionHistory).toList());
                });

        // 4. 환전 이력 (해당 계좌에 관련된 것만)
        if (!relevantAccountIds.isEmpty()) {
            List<ExchangeHistory> exList =
                    exhRepo.findByFromAccount_IdInOrToAccount_IdIn(relevantAccountIds, relevantAccountIds);
            resultList.addAll(
                    exList.stream()
                            .map(e -> {
                                Account matchedAccount = relevantAccounts.stream()
                                        .filter(acc ->
                                                acc.getId().equals(e.getFromAccount().getId()) ||
                                                        acc.getId().equals(e.getToAccount().getId()))
                                        .findFirst()
                                        .orElse(null);
                                return exhService.getExchangeHistory(e, matchedAccount);
                            })
                            .toList()
            );
        }

        // 5. 최신순 정렬
        resultList.sort(Comparator.comparing(GetTransactionHistoryResponse::getTransferredAt).reversed());

        // 6. 수동 페이징 처리
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