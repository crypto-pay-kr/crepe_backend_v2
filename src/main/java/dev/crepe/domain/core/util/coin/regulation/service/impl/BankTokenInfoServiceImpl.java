package dev.crepe.domain.core.util.coin.regulation.service.impl;

import dev.crepe.domain.admin.dto.response.GetAllBankTokenResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.coin.regulation.exception.BankTokenNotFoundException;
import dev.crepe.domain.core.util.coin.regulation.exception.TokenAlreadyRequestedException;
import dev.crepe.domain.core.util.coin.regulation.model.dto.request.TokenInfoResponse;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.repository.PortfolioRepository;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenInfoService;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BankTokenInfoServiceImpl implements BankTokenInfoService {

    private final ExceptionDbService exceptionDbService;
    private final BankTokenRepository bankTokenRepository;
    private final PortfolioRepository portfolioRepository;
    private final AccountRepository accountRepository;

    @Override
    public TokenInfoResponse getTokenInfo(String currency) {
        // 1. 토큰 조회
        BankToken token = bankTokenRepository.findByCurrency(currency)
                .orElseThrow(() -> exceptionDbService.getException("BANK_TOKEN_001"));

        // 2. 포트폴리오 조회
        List<Portfolio> portfolios = portfolioRepository.findAllByBankToken_Currency(currency);

        // 3. 은행 HTK 계좌 조회 (토큰 잔액 확인용)
        Account tokenAccount = accountRepository.findByBankToken_CurrencyAndActorIsNull(currency)
                .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001"));

        // 4. 포트폴리오 내 각 코인별로, 은행 계좌 잔액 조회
        List<Long> coinIds = portfolios.stream()
                .map(p -> p.getCoin().getId())
                .toList();

        List<Account> bankCoinAccounts = accountRepository.findByBank_IdAndCoin_IdIn(token.getBank().getId(), coinIds);

        Map<String, BigDecimal> bankCoinBalanceMap = bankCoinAccounts.stream()
                .collect(Collectors.toMap(
                        acc -> acc.getCoin().getCurrency(),
                        Account::getBalance
                ));

        // 5. 포트폴리오 응답 생성
        List<TokenInfoResponse.PortfolioItem> portfolioItems = portfolios.stream()
                .map(p -> TokenInfoResponse.PortfolioItem.builder()
                        .currency(p.getCoin().getCurrency())
                        .amount(p.getAmount())
                        .nonAvailableAmount(bankCoinBalanceMap.getOrDefault(p.getCoin().getCurrency(), BigDecimal.ZERO))
                        .build())
                .toList();

        // 6. 최종 응답 반환
        return TokenInfoResponse.builder()
                .Currency(token.getCurrency())
                .totalSupply(token.getTotalSupply())
                .tokenBalance(tokenAccount.getBalance())
                .portfolios(portfolioItems).build();

    }

    @Override
    public void validateTokenNotAlreadyRequested(Long bankId) {
        if (bankTokenRepository.existsByBank_Id(bankId)) {
            throw exceptionDbService.getException("BANK_TOKEN_002");
        }
    }

    @Override
    public List<BankToken> findAllBankTokens(PageRequest pageRequest) {
        return bankTokenRepository.findAll(pageRequest).getContent();
    }


    @Override
    public BankToken findByBank(Bank bank) {
        return bankTokenRepository.findByBank(bank)
                .orElseThrow(() -> exceptionDbService.getException("BANK_TOKEN_001"));
    }

    @Override
    public void saveBankToken(BankToken bankToken) {  bankTokenRepository.save(bankToken);}

}