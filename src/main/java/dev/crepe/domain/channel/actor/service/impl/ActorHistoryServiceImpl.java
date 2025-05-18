package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.channel.actor.service.ActorHistoryService;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
<<<<<<< HEAD
=======
import dev.crepe.domain.core.util.history.global.service.HistoryService;
>>>>>>> 43a00873af0c028c14364283582a9ca285852c7e
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActorHistoryServiceImpl implements ActorHistoryService {

    private final HistoryService historyService;

    @Override
    public Slice<GetTransactionHistoryResponse> getNonRegulationHistory(String email, String currency, int page, int size) {
        return historyService.getNonRegulationHistory(email, currency, page, size);
    }
}