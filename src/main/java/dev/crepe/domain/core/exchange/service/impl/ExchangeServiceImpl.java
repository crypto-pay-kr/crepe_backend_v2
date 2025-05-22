package dev.crepe.domain.core.exchange.service.impl;

import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.exception.NotEnoughAmountException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

    private final AccountRepository accountRepository;
    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final PortfolioRepository portfolioRepository;
    private final ExchangeValidateServiceImpl validator;
    private final AccountService accountService;

    @Override
    @Transactional
    public void exchangeToToken(String email, CreateExchangeRequest request) {

        // 1. 계좌 조회
        ExchangeAccountsResponse accounts = getExchangeAccounts(email, request, true);

        // 2. 사용자 코인 계좌의 잔액이 요청 수량보다 부족한지 검증
        if (accounts.getActorCoinAccount().getBalance().compareTo(request.getCoinAmount()) < 0) {
            throw new NotEnoughAmountException("사용자 코인계좌에 코인이 부족하여 환전할 수 없습니다");
        }

        // 3. 은행이 지급할 수 있는 토큰 충분한지 검증
        if (accounts.getBankTokenAccount().getBalance().compareTo(request.getTokenAmount()) < 0) {
            throw new NotEnoughAmountException("은행 계좌의 토큰이 부족하여 환전할 수 없습니다.");
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
        accounts.getBankTokenAccount().reduceAmount(request.getTokenAmount());

        // 사용자 입장 : 코인 수량 감소, HTK 수량 증가
        accounts.getActorCoinAccount().reduceAmount(request.getCoinAmount());
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

        // 1. 계좌 조회
        ExchangeAccountsResponse accounts= getExchangeAccounts(email, request,false);

        // 2. 사용자 코인 계좌의 잔액이 요청 수량보다 부족한지 검증
        if (accounts.getBankTokenAccount().getBalance().compareTo(request.getTokenAmount()) < 0) {
            throw new NotEnoughAmountException("사용자의 토큰양이 부족하여 환전 할 수 없습니다");
        }

        // 3. 은행이 지급할 수 있는 토큰 충분한지 검증
        if (accounts.getBankCoinAccount().getBalance().compareTo(request.getCoinAmount()) < 0) {
            throw new NotEnoughAmountException("은행의 코인이 부족하여 환전할 수 없습니다.");
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
        accounts.getBankCoinAccount().reduceAmount(request.getCoinAmount());
        // 유저입장 : 토큰 보유량 감소, 코인 수량 증가
        accounts.getActorTokenAccount().reduceAmount(request.getTokenAmount());
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

        Account actorCoinAccount = accountRepository.findByActor_EmailAndCoin_Currency(email, isCoinToToken ? fromCurrency: toCurrency)
                .orElseThrow(AccountNotFoundException::new);

        Account actorTokenAccount = accountRepository.findByActor_EmailAndBankToken_Currency(email, isCoinToToken ? toCurrency: fromCurrency)
                .orElseGet(() -> accountService.getOrCreateTokenAccount(email, toCurrency));

        Account bankTokenAccount = accountRepository.findByBankToken_CurrencyAndActorIsNull(
                        isCoinToToken ? toCurrency : fromCurrency)
                .orElseThrow(AccountNotFoundException::new);

        List<Portfolio> portfolios = portfolioRepository.findAllByBankToken_Currency(
                isCoinToToken ? toCurrency : fromCurrency);

        List<Long> coinIds = portfolios.stream()
                .map(p -> p.getCoin().getId())
                .toList();

        List<Account> bankCoinAccounts = accountRepository.findByBank_IdAndCoin_IdIn(bankTokenAccount.getBank().getId(), coinIds);
        Account bankCoinAccount = bankCoinAccounts.stream()
                .filter(acc -> acc.getCoin().getCurrency().equalsIgnoreCase(isCoinToToken? fromCurrency: toCurrency))
                .findFirst()
                .orElseThrow(AccountNotFoundException::new);

        return new ExchangeAccountsResponse(actorCoinAccount, actorTokenAccount, bankTokenAccount, bankCoinAccount, portfolios, bankCoinAccounts);
    }
}
