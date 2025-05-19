package dev.crepe.domain.core.util.history.token.service;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.token.model.TokenRequestType;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TokenHistoryService {
    TokenHistory findById(Long tokenHistoryId);

    Optional<TokenHistory> findByBankTokenAndStatus(BankToken bankToken, BankTokenStatus status);

    List<TokenHistory> findTokenHistoriesByBank(Bank bank, PageRequest pageRequest);

    TokenHistory createTokenHistory(BankToken bankToken, BigDecimal totalSupplyAmount, BankTokenStatus status, TokenRequestType requestType);
}
