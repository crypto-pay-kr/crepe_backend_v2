package dev.crepe.domain.core.util.history.token.service.impl;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.exception.TokenHistoryNotFoundException;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.TokenHistoryRepository;
import dev.crepe.domain.core.util.history.token.service.TokenHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenHistoryServiceImpl implements TokenHistoryService {

    private final TokenHistoryRepository tokenHistoryRepository;

    @Override
    public TokenHistory findById(Long tokenHistoryId) {
        return tokenHistoryRepository.findById(tokenHistoryId)
                .orElseThrow(() -> new TokenHistoryNotFoundException(tokenHistoryId));
    }

    @Override
    public Optional<TokenHistory> findByBankTokenAndStatus(BankToken bankToken, BankTokenStatus status) {
        return tokenHistoryRepository.findByBankTokenAndStatus(bankToken, status);
    }

    @Override
    public List<TokenHistory> findTokenHistoriesByBank(Bank bank, PageRequest pageRequest) {
        return tokenHistoryRepository.findByBankToken_Bank_Id(bank.getId(), pageRequest).getContent();
    }
}