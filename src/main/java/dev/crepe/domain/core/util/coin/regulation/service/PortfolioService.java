package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;

import java.util.List;
import java.util.Optional;

public interface PortfolioService {

    void validatePortfolioConstitute(List<CreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail);

    void revalidatePortfolioConstitute(List<ReCreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail);

    void savePortfolios(CreateBankTokenRequest request, BankToken bankToken);

    void clearPortfolios(BankToken bankToken);

    void createPortfolios(BankToken bankToken, TokenHistory pendingTokenHistory);

    Optional<Portfolio> findByBankTokenAndCoinCurrency(BankToken bankToken, String currency);


}
