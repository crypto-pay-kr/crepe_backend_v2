package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import org.springframework.data.domain.Slice;

public interface ActorHistoryService {
    Slice<GetTransactionHistoryResponse> getNonRegulationHistory(String email, String currency, int page, int size);

    Slice<GetTransactionHistoryResponse> getTokenHistory(String email, String currency, int page, int size);
}