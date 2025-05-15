package dev.crepe.domain.core.util.history.token.service;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.token.model.entity.TokenPortfolioHistory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface PortfolioHistoryService {

    TokenPortfolioHistory addTokenPortfolioHistory(BankToken bankToken, BigDecimal amount, String changeReason);
}
