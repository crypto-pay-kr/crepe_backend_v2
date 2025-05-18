package dev.crepe.domain.core.util.history.token.service.impl;

import dev.crepe.domain.core.util.coin.regulation.exception.TokenHistoryNotFoundException;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.TokenHistoryRepository;
import dev.crepe.domain.core.util.history.token.service.TokenHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenHistoryServiceImpl implements TokenHistoryService {

    private final TokenHistoryRepository tokenHistoryRepository;

    @Override
    public TokenHistory findById(Long tokenHistoryId) {
        return tokenHistoryRepository.findById(tokenHistoryId)
                .orElseThrow(() -> new TokenHistoryNotFoundException(tokenHistoryId));
    }
}