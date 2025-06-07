package dev.crepe.domain.core.util.history.token.service.impl;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.token.model.TokenRequestType;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.TokenHistoryRepository;
import dev.crepe.domain.core.util.history.token.service.TokenHistoryService;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenHistoryServiceImpl implements TokenHistoryService {

    private final ExceptionDbService exceptionDbService;
    private final TokenHistoryRepository tokenHistoryRepository;

    @Override
    public TokenHistory findById(Long tokenHistoryId) {
        return tokenHistoryRepository.findById(tokenHistoryId)
                .orElseThrow(() -> exceptionDbService.getException("BANK_TOKEN_003"));
    }

    @Override
    public Optional<TokenHistory> findByBankTokenAndStatus(BankToken bankToken, BankTokenStatus status) {
        return tokenHistoryRepository.findByBankTokenAndStatus(bankToken, status);
    }

    @Override
    public List<TokenHistory> findTokenHistoriesByBank(Bank bank, PageRequest pageRequest) {
        return tokenHistoryRepository.findByBankToken_Bank_Id(bank.getId(), pageRequest).getContent();
    }

    @Override
    public TokenHistory createTokenHistory(BankToken bankToken, BigDecimal totalSupplyAmount, BankTokenStatus status, TokenRequestType requestType) {
        TokenHistory tokenHistory = TokenHistory.builder()
                .bankToken(bankToken)
                .totalSupplyAmount(totalSupplyAmount)
                .status(status)
                .requestType(requestType)
                .build();
        return tokenHistoryRepository.save(tokenHistory);
    }
}