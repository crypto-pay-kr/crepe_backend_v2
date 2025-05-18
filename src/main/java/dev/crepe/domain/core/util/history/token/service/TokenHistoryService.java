package dev.crepe.domain.core.util.history.token.service;

import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;

public interface TokenHistoryService {
    TokenHistory findById(Long tokenHistoryId);
}
