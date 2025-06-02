package dev.crepe.domain.core.util.coin.regulation.service.impl;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenInfoService;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioService;
import dev.crepe.domain.core.util.coin.regulation.service.TokenPriceService;
import dev.crepe.domain.core.util.coin.regulation.util.TokenCalculationUtil;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.service.TokenSetupService;
import dev.crepe.domain.core.util.history.token.service.PortfolioHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenSetupServiceImpl implements TokenSetupService {

    private final PortfolioHistoryService portfolioHistoryService;
    private final TokenPriceService tokenPriceService;
    private final AccountService accountService;
    private final BankTokenInfoService bankTokenInfoService;
    private final PortfolioService portfolioService;
    private final TokenCalculationUtil tokenCalculationUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BankToken requestTokenGenerate(CreateBankTokenRequest request, Bank bank) {
        // 중복 토큰 요청 검증
        bankTokenInfoService.validateTokenNotAlreadyRequested(bank.getId());

        BigDecimal totalSupplyAmount = tokenCalculationUtil.calculateTotalPrice(request);

        BankToken bankToken = BankToken.builder()
                .bank(bank)
                .name(request.getTokenName())
                .currency(request.getTokenCurrency())
                .totalSupply(totalSupplyAmount)
                .status(BankTokenStatus.PENDING)
                .build();
        bankTokenInfoService.saveBankToken(bankToken);

        // TokenPrice 생성 및 저장
        tokenPriceService.createAndSaveTokenPrice(bankToken, totalSupplyAmount);

        // 포트폴리오 저장
        portfolioService.savePortfolios(request, bankToken);

        log.info("예상 총 발행량: {}", totalSupplyAmount);

        // 토큰 및 포토폴리오 내역 추가
        portfolioHistoryService.addTokenPortfolioHistory(request, bankToken, totalSupplyAmount);
        return bankToken;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BankToken requestTokenReGenerate(ReCreateBankTokenRequest request, Bank bank) {
        // 기존 토큰 조회
        BankToken bankToken = bankTokenInfoService.findByBank(bank);

        // BankToken 계좌 조회
        Account bankTokenAccount = accountService.findBankTokenAccount(bank.getId(), bankToken);

        // 유통 중인 토큰량 계산
        BigDecimal circulatingSupply = tokenCalculationUtil.getCirculatingSupply(bankTokenAccount);

        // 요청된 예상 토큰량 계산
        BigDecimal expectedTotal = tokenCalculationUtil.calculateTotalPrice(request);

        // 포트폴리오 변경 조건 충족 여부 검증
        tokenCalculationUtil.validatePortfolioChange(request, bankToken, circulatingSupply);

        // 포트폴리오 안전성 검증
        tokenCalculationUtil.validatePortfolioSafety(request, circulatingSupply, BigDecimal.valueOf(1.1));

        // 토큰 변경 내역 업데이트
        portfolioHistoryService.updateTokenHistory(request, bankToken, expectedTotal);

        return bankToken;
    }
}