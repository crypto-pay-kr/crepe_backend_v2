package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.util.history.transfer.model.dto.GetTransactionHistoryResponse;
import org.springframework.data.domain.Slice;

public interface ActorHistoryService {
    Slice<GetTransactionHistoryResponse> getTransactionHistory(String email, String currency, int page, int size);
}