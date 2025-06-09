package dev.crepe.domain.core.util.history.global.service;

import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import org.springframework.data.domain.Slice;

public interface HistoryService {


    Slice<GetTransactionHistoryResponse> getNonRegulationHistoryOptimizedCache(String email, String currency, int page, int size);
}
