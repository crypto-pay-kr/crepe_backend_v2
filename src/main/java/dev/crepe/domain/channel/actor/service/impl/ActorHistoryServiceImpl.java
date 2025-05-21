package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.channel.actor.service.ActorHistoryService;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import dev.crepe.domain.core.util.history.exchange.service.ExchangeHistoryService;
import dev.crepe.domain.core.util.history.global.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActorHistoryServiceImpl implements ActorHistoryService {

    private final HistoryService historyService;
    private final ExchangeHistoryService exchangeHistoryService;

    @Override
    public Slice<GetTransactionHistoryResponse> getNonRegulationHistory(String email, String currency, int page, int size) {
        return historyService.getNonRegulationHistory(email, currency, page, size);
    }

    @Override
    public Slice<GetTransactionHistoryResponse> getTokenHistory(String email, String currency, int page, int size) {
        return exchangeHistoryService.getExchangeHistoryList(email, currency, page, size);
    }
}