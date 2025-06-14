package dev.crepe.domain.core.exchange.service.impl;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.exchange.model.dto.request.CreateExchangeRequest;
import dev.crepe.domain.core.exchange.model.dto.response.ExchangeAccountsResponse;
import dev.crepe.domain.core.exchange.service.ExchangeService;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.repository.PortfolioRepository;
import dev.crepe.domain.core.util.history.exchange.model.entity.ExchangeHistory;
import dev.crepe.domain.core.util.history.exchange.repositroy.ExchangeHistoryRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

    private final AccountRepository accountRepository;
    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final PortfolioRepository portfolioRepository;
    private final ExchangeValidateServiceImpl validator;
    private final AccountService accountService;
    private final ExceptionDbService exceptionDbService;
    @Override
    @Transactional
    public void exchangeToToken(String email, CreateExchangeRequest request) {
        log.info("코인에서 토큰으로 환전 요청 처리 시작: {}", email);
        // 1. 계좌 조회
        ExchangeAccountsResponse accounts = getExchangeAccounts(email, request, true);

        // 2. 사용자 코인 계좌의 잔액이 요청 수량보다 부족한지 검증
        if (accounts.getActorCoinAccount().getBalance().compareTo(request.getCoinAmount()) < 0) {
          throw exceptionDbService.getException("ACCOUNT_006");
        }

        // 3. 은행이 지급할 수 있는 토큰 충분한지 검증
        if (accounts.getBankTokenAccount().getBalance().compareTo(request.getTokenAmount()) < 0) {
            throw exceptionDbService.getException("BANK_ACCOUNT_001");
        }
        // 4. 은행의 해당 코인 계좌에 포트폴리오 수량 이상으로 들어가지 않도록 검증
        Optional<Portfolio> matchedPortfolio = accounts.getPortfolios().stream()
                .filter(p -> p.getCoin().getCurrency().equals(request.getFromCurrency()))
                .findFirst();


        BigDecimal portfolioMaxAmount = matchedPortfolio.get().getAmount(); // 예: 200 XRP
        BigDecimal currentBankCoinBalance = accounts.getBankCoinAccount().getBalance(); // 현재 은행 보유량
        BigDecimal afterBalance = currentBankCoinBalance.add(request.getCoinAmount());

        if (afterBalance.compareTo(portfolioMaxAmount) > 0) {
            throw exceptionDbService.getException("BANK_ACCOUNT_001"); // 제한 초과
        }
        // 4. 환전 수량 계산 (전체 자산 기준 환산 비율로 HTK 수량 계산)
        BigDecimal result = validator.validateRequestedTokenAmount(
                request,
                accounts.getPortfolios(),
                accounts.getBankTokenAccount().getBalance(),
                accounts.getBankCoinAccounts()
        );

        // 5. 자금 이동 (코인 → 토큰 환전)
        // 은행 입장 : 코인 수량 증가, 토큰 보유량 감소
        accounts.getBankCoinAccount().addAmount(request.getCoinAmount());
        accountService.validateAndReduceAmount(accounts.getBankTokenAccount(), request.getTokenAmount());

        // 사용자 입장 : 코인 수량 감소, HTK 수량 증가
        accountService.validateAndReduceAmount(accounts.getActorCoinAccount(), request.getCoinAmount());
        accounts.getActorTokenAccount().addAmount(request.getTokenAmount());

        // 6. 환전 내역 저장
        ExchangeHistory exchangeHistory = ExchangeHistory.builder()
                .fromAccount(accounts.getActorCoinAccount())
                .toAccount(accounts.getBankTokenAccount())
                .fromAmount(request.getCoinAmount())
                .toAmount(request.getTokenAmount())
                .fromExchangeRate(request.getCoinRates().get(request.getFromCurrency()))
                .toExchangeRate(result)
                .afterCoinBalanceFrom(accounts.getActorCoinAccount().getBalance())
                .afterTokenBalanceFrom(accounts.getActorTokenAccount().getBalance())
                .afterTokenBalanceTo(accounts.getBankTokenAccount().getBalance())
                .afterCoinBalanceTo(accounts.getBankCoinAccount().getBalance())
                .build();

        exchangeHistoryRepository.save(exchangeHistory);
    }


    @Override
    @Transactional
    public void exchangeToCoin(String email, CreateExchangeRequest request) {
        log.info("토큰에서 코인으로 환전 요청 처리 시작: {}", email);
        // 1. 계좌 조회
        ExchangeAccountsResponse accounts= getExchangeAccounts(email, request,false);

        // 2. 사용자 코인 계좌의 잔액이 요청 수량보다 부족한지 검증
        if (accounts.getBankTokenAccount().getBalance().compareTo(request.getTokenAmount()) < 0) {
            throw exceptionDbService.getException("ACCOUNT_006");
        }

        // 3. 은행이 지급할 수 있는 토큰 충분한지 검증
        if (accounts.getBankCoinAccount().getBalance().compareTo(request.getCoinAmount()) < 0) {
           throw exceptionDbService.getException("ACCOUNT_006");
        }

        // 3. 시세 검증 및 교환량 계산
        BigDecimal result = validator.validateRequestedCoinAmount(
                request,
                accounts.getPortfolios(),
                accounts.getBankTokenAccount().getBalance(),
                accounts.getBankCoinAccounts()
        );

        // 4. 자금 이동 (토큰 -> 코인)
        // 은행 입장 : 토큰 보유량 증가, 코인 수량감소
        accounts.getBankTokenAccount().addAmount(request.getTokenAmount());
        accountService.validateAndReduceAmount(accounts.getBankCoinAccount(), request.getCoinAmount());
        // 유저입장 : 토큰 보유량 감소, 코인 수량 증가
        accountService.validateAndReduceAmount(accounts.getActorTokenAccount(), request.getTokenAmount());
        accounts.getActorCoinAccount().addAmount(request.getCoinAmount());

        // 5. 환전 기록 저장
        ExchangeHistory exchangeHistory = ExchangeHistory.builder()
                .fromAccount(accounts.getBankTokenAccount())
                .toAccount(accounts.getActorCoinAccount())
                .fromAmount(request.getTokenAmount())
                .toAmount(request.getCoinAmount())
                .fromExchangeRate(result)
                .toExchangeRate(request.getCoinRates().getOrDefault(request.getToCurrency(), BigDecimal.ZERO))
                .afterTokenBalanceFrom(accounts.getBankTokenAccount().getBalance())
                .afterCoinBalanceFrom(accounts.getBankCoinAccount().getBalance())
                .afterCoinBalanceTo(accounts.getActorCoinAccount().getBalance())
                .afterTokenBalanceTo(accounts.getActorTokenAccount().getBalance())
                .build();

        exchangeHistoryRepository.save(exchangeHistory);

    }
    private ExchangeAccountsResponse getExchangeAccounts(String email, CreateExchangeRequest request, boolean isCoinToToken) {
        String fromCurrency = request.getFromCurrency();
        String toCurrency = request.getToCurrency();

        Account actorCoinAccount = accountRepository.findCoinAccountWithLock(email, isCoinToToken ? fromCurrency: toCurrency)
                .orElseThrow(()->exceptionDbService.getException("ACCOUNT_001"));

        Account actorTokenAccount = accountRepository.findTokenAccountWithLock(email, isCoinToToken ? toCurrency: fromCurrency)
                .orElseGet(() -> accountService.getOrCreateTokenAccount(email, toCurrency));

        Account bankTokenAccount = accountRepository.findBankTokenAccountWithLock(
                        isCoinToToken ? toCurrency : fromCurrency)
                .orElseThrow(()->exceptionDbService.getException("ACCOUNT_001"));

        List<Portfolio> portfolios = portfolioRepository.findAllByBankToken_Currency(
                isCoinToToken ? toCurrency : fromCurrency);

        List<Long> coinIds = portfolios.stream()
                .map(p -> p.getCoin().getId())
                .toList();

        List<Account> bankCoinAccounts = accountRepository.findByBankCoinAccountWithLock(bankTokenAccount.getBank().getId(), coinIds);
        Account bankCoinAccount = bankCoinAccounts.stream()
                .filter(acc -> acc.getCoin().getCurrency().equalsIgnoreCase(isCoinToToken? fromCurrency: toCurrency))
                .findFirst()
                .orElseThrow(()->exceptionDbService.getException("ACCOUNT_001"));

        return new ExchangeAccountsResponse(actorCoinAccount, actorTokenAccount, bankTokenAccount, bankCoinAccount, portfolios, bankCoinAccounts);
    }
}
