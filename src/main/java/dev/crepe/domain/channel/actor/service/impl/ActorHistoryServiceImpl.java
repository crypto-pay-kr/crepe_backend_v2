package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.channel.actor.service.ActorHistoryService;
import dev.crepe.domain.core.util.history.transfer.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.transfer.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActorHistoryServiceImpl implements ActorHistoryService {

    private final TransactionHistoryService transactionHistoryService;

    @Override
    public Slice<GetTransactionHistoryResponse> getTransactionHistory(String email, String currency, int page, int size) {
        return transactionHistoryService.getTransactionHistory(email, currency, page, size);
    }
}