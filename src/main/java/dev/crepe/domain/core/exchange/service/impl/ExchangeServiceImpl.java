package dev.crepe.domain.core.exchange.service.impl;

import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.exception.NotEnoughAmountException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.exchange.model.dto.GetExchangeRequest;
import dev.crepe.domain.core.exchange.service.ExchangeService;
import dev.crepe.domain.core.exchange.util.ExchangeCalculator;
import dev.crepe.domain.core.exchange.util.ExchangeValidator;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.repository.PortfolioRepository;
import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import dev.crepe.domain.core.util.history.exchange.repositroy.ExchangeHistoryRepository;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

    private final AccountRepository accountRepository;
    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final UpbitExchangeService upbitExchangeService;
    private final PortfolioRepository portfolioRepository;
    private final ExchangeValidator exchangeValidator;
    private final ExchangeCalculator exchangeCalculator;

    @Override
    @Transactional
    public void exchangeToToken(String email, GetExchangeRequest request) {

        // 1. 계좌 조회
        Account fromCoinAccount = accountRepository.findByActor_EmailAndCoin_Currency(email, request.getFromCurrency())
                .orElseThrow(AccountNotFoundException::new);
        Account fromTokenAccount = accountRepository.findByActor_EmailAndBankToken_Currency(email, request.getToCurrency())
                .orElseThrow(AccountNotFoundException::new);
        Account toTokenAccount = accountRepository.findByBankToken_Currency(request.getToCurrency())
                .orElseThrow(AccountNotFoundException::new);
        Account toCoinAccount = accountRepository.findByBankToken_BankAndCoin_Currency(toTokenAccount.getBankToken().getBank(), request.getToCurrency())
                .orElseThrow(AccountNotFoundException::new);
        // 2. 잔액 확인
        if (fromCoinAccount.getBalance().compareTo(request.getCoinAmount()) < 0) {
            throw new NotEnoughAmountException(request.getFromCurrency());
        }
        // 3.프론트에서 보내온 실시간 시세 검증
        exchangeValidator.validateRates(request.getCoinRates(), upbitExchangeService);

        // 4. 포트폴리오 조회 및 자본금 계산
        List<Portfolio> portfolios = portfolioRepository.findAllByBankToken_Currency(request.getToCurrency())
        Map<String, BigDecimal> coinRates = request.getCoinRates();
        BigDecimal coinToKRW = exchangeCalculator.calculateTotalCapitalKRW(portfolios, coinRates);
        BigDecimal tokenPrice = exchangeCalculator.calculateTokenPrice(coinToKRW, toTokenAccount.getBankToken().getTotalSupply());

        // 5. 교환할 토큰 수량 계산
        BigDecimal toAmount = exchangeCalculator.calculateTokenAmount(
                request.getCoinAmount(),
                request.getCoinRates().get(request.getFromCurrency()),
                tokenPrice
        );

        // 6. 요청한 자본금과 일치 확인
        exchangeValidator.assertEquals(toAmount, request.getTokenAmount());

        // 7. 자금 이동
        fromCoinAccount.reduceAmount(request.getCoinAmount());
        fromTokenAccount.addAmount(toAmount);
        toCoinAccount.addAmount(request.getCoinAmount());
        toTokenAccount.reduceAmount(toAmount);

        // 8. 환전 기록 저장
        ExchangeHistory exchangeHistory = ExchangeHistory.builder()
                .fromAccount(fromCoinAccount)
                .toAccount(toTokenAccount)
                .fromAmount(request.getCoinAmount())
                .toAmount(toAmount)
                .fromExchangeRate(request.getCoinRates().get(request.getFromCurrency()))
                .toExchangeRate(tokenPrice)
                .afterBalanceFrom(fromCoinAccount.getBalance())
                .afterBalanceTo(fromTokenAccount.getBalance())
                .build();

        exchangeHistoryRepository.save(exchangeHistory);
    }


    @Override
    @Transactional
    public void exchangeToCoin(String email, GetExchangeRequest request) {
        // 1. 계좌 조회
        Account fromTokenAccount = accountRepository.findByActor_EmailAndBankToken_Currency(email, request.getFromCurrency())
                .orElseThrow(AccountNotFoundException::new);
        Account toCoinAccount = accountRepository.findByActor_EmailAndCoin_Currency(email, request.getToCurrency())
                .orElseThrow(AccountNotFoundException::new);
        Account bankTokenAccount = accountRepository.findByBankToken_Currency(request.getFromCurrency())
                .orElseThrow(AccountNotFoundException::new);
        Account bankCoinAccount = accountRepository.findByBankToken_BankAndCoin_Currency(
                        bankTokenAccount.getBankToken().getBank(), request.getToCurrency())
                .orElseThrow(AccountNotFoundException::new);

        // 2. 잔액 확인
        if (fromTokenAccount.getBalance().compareTo(request.getTokenAmount()) < 0) {
            throw new NotEnoughAmountException(request.getFromCurrency());
        }

        // 3. 시세 검증
        exchangeValidator.validateRates(request.getCoinRates(), upbitExchangeService);

        // 4. 포트폴리오 조회 및 자본금 계산
        List<Portfolio> portfolios = portfolioRepository.findAllByBankToken_Currency(request.getFromCurrency());
        Map<String, BigDecimal> coinRates = request.getCoinRates();
        BigDecimal totalCapital = exchangeCalculator.calculateTotalCapitalKRW(portfolios, request.getCoinRates());
        BigDecimal tokenPrice = exchangeCalculator.calculateTokenPrice(totalCapital, fromTokenAccount.getBankToken().getTotalSupply());

        // 5. 환전 대상 코인 환산
        BigDecimal tokenKRW = request.getTokenAmount().multiply(tokenPrice);
        BigDecimal coinRate = coinRates.get(request.getToCurrency());

        //6. 해당 코인의 자본금 내 원화 가치 계산
        BigDecimal coinValueInCapital = portfolios.stream()
                .filter(p -> p.getCoin().getCurrency().equalsIgnoreCase(request.getToCurrency()))
                .findFirst()
                .map(p -> p.getAmount().multiply(coinRates.get(p.getCoin().getCurrency())))
                .orElseThrow(() -> new IllegalArgumentException("해당 코인은 포트폴리오에 없습니다."));

        // 7. 교환 할 코인 수량 계산
        BigDecimal coinAmount = exchangeCalculator.calculateCoinAmount(
                tokenKRW,
                coinValueInCapital,
                totalCapital,
                coinRate
        );

        // 7. 요청 값과 검증
        exchangeValidator.assertEquals(coinAmount, request.getCoinAmount());

        // 8. 자금 이동
        fromTokenAccount.reduceAmount(request.getTokenAmount());
        toCoinAccount.addAmount(coinAmount);
        bankTokenAccount.addAmount(request.getTokenAmount());
        bankCoinAccount.reduceAmount(coinAmount);

        // 9. 기록 저장
        ExchangeHistory exchangeHistory = ExchangeHistory.builder()
                .fromAccount(fromTokenAccount)
                .toAccount(toCoinAccount)
                .fromAmount(request.getTokenAmount())
                .toAmount(coinAmount)
                .fromExchangeRate(tokenPrice)
                .toExchangeRate(coinRate)
                .afterBalanceFrom(fromTokenAccount.getBalance())
                .afterBalanceTo(toCoinAccount.getBalance())
                .build();

        exchangeHistoryRepository.save(exchangeHistory);
    }
}
