package dev.crepe.domain.core.util.history.token.service;

import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;


import java.math.BigDecimal;

public interface PortfolioHistoryService {


    void addTokenPortfolioHistory(CreateBankTokenRequest request, BankToken bankToken, BigDecimal totalSupplyAmount);

    void updateTokenHistory(ReCreateBankTokenRequest request, BankToken bankToken, BigDecimal totalSupplyAmount);

    void updateTokenHistoryStatus(Long tokenHistoryId, BankTokenStatus status);

    void updateTokenHistoryStatus(RejectBankTokenRequest request, Long tokenHistoryId, BankTokenStatus status);

    void updatePortfolio(BankToken bankToken);
}
