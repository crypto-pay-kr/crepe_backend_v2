package dev.crepe.domain.core.util.history.exchange.service.impl;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import dev.crepe.domain.core.util.history.exchange.service.ExchangeHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExchangeHistoryServiceImpl implements ExchangeHistoryService {

    @Override
    public GetTransactionHistoryResponse getExchangeHistory(ExchangeHistory ex, Account userAccount) {
        boolean isSender = ex.getFromAccount().getId().equals(userAccount.getId());

        BigDecimal rawAmount = isSender ? ex.getFromAmount() : ex.getToAmount();
        BigDecimal signedAmount = isSender ? rawAmount.negate() : rawAmount;

        return GetTransactionHistoryResponse.builder()
                .type("EXCHANGE")
                .status("ACCEPTED")
                .amount(signedAmount)
                .afterBalance(isSender ? ex.getAfterBalanceFrom() : ex.getAfterBalanceTo())
                .transferredAt(ex.getCreatedAt())
                .build();
    }
}