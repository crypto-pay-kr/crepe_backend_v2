package dev.crepe.domain.core.util.history.token.service;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.token.model.entity.TokenPortfolioHistory;
import dev.crepe.domain.core.util.history.token.repository.PortfolioHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioHistoryServiceImpl implements  PortfolioHistoryService{

    private final PortfolioHistoryRepository portfolioHistoryRepository;

    public TokenPortfolioHistory addTokenPortfolioHistory(BankToken bankToken, BigDecimal amount, String changeReason) {
        TokenPortfolioHistory history = TokenPortfolioHistory.builder()
                .bankToken(bankToken)
                .amount(amount)
                .changeReason(changeReason)
                .build();
        return portfolioHistoryRepository.save(history);
    }
}
