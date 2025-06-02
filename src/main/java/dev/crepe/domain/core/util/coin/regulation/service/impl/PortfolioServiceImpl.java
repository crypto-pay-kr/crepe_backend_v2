package dev.crepe.domain.core.util.coin.regulation.service.impl;


import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.service.impl.CoinServiceImpl;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.repository.PortfolioRepository;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioService;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private static final int MIN_PORTFOLIO_SIZE = 2;

    private final AccountService accountService;
    private final CoinServiceImpl coinService;
    private final PortfolioRepository portfolioRepository;
    private final ExceptionDbService exceptionDbService;

    // 포토폴리오 구성 유효성 검증
    @Override
    public void validatePortfolioConstitute(List<CreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail) {

        if (coinInfoList == null || coinInfoList.size() < MIN_PORTFOLIO_SIZE) {
            throw exceptionDbService.getException("PORTFOLIO_006");
        }

        for (CreateBankTokenRequest.CoinInfo coin : coinInfoList) {
            if (coin.getAmount() == null || coin.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw exceptionDbService.getException("PORTFOLIO_005");
            }

            accountService.findActiveAccountByBankEmailAndCurrency(bankEmail, coin.getCurrency());

        }

    }


    // 포토폴리오 재구성 유효성 검증
    @Override
    public void revalidatePortfolioConstitute(List<ReCreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail) {

        if (coinInfoList == null || coinInfoList.size() < MIN_PORTFOLIO_SIZE) {
            throw exceptionDbService.getException("PORTFOLIO_007");
        }

        for (ReCreateBankTokenRequest.CoinInfo coin : coinInfoList) {
            if (coin.getAmount() == null || coin.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw exceptionDbService.getException("PORTFOLIO_005");
            }

            // 새로운 계좌 추가 시 계좌가 있는지 확인
            accountService.findActiveAccountByBankEmailAndCurrency(bankEmail, coin.getCurrency());
        }
    }

    @Override
    public void savePortfolios(CreateBankTokenRequest request, BankToken bankToken) {
        request.getPortfolioCoins().forEach(coinInfo -> {
            Coin coin = coinService.findByCurrency(coinInfo.getCurrency());
            Portfolio portfolio = Portfolio.builder()
                    .bankToken(bankToken)
                    .coin(coin)
                    .amount(coinInfo.getAmount())
                    .initialPrice(coinInfo.getCurrentPrice())
                    .build();
            portfolioRepository.save(portfolio);
        });
    }

    @Override
    public void clearPortfolios(BankToken bankToken) {
        bankToken.getPortfolios().clear();
        portfolioRepository.deleteAllByBankToken(bankToken);
    }

    @Override
    public void createPortfolios(BankToken bankToken, TokenHistory pendingTokenHistory) {
        pendingTokenHistory.getPortfolioDetails().forEach(portfolio -> {
            Coin coin = coinService.findByCurrency(portfolio.getCoinCurrency());
            Portfolio newPortfolio = Portfolio.builder()
                    .bankToken(bankToken)
                    .coin(coin)
                    .amount(portfolio.getUpdateAmount())
                    .initialPrice(portfolio.getUpdatePrice())
                    .build();
            portfolioRepository.save(newPortfolio);
        });
    }


    @Override
    public Optional<Portfolio> findByBankTokenAndCoinCurrency(BankToken bankToken, String currency) {
        return portfolioRepository.findByBankTokenAndCoinCurrency(bankToken, currency);
    }

}
