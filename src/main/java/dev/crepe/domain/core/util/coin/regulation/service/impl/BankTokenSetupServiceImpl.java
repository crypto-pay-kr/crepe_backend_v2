package dev.crepe.domain.core.util.coin.regulation.service.impl;

import dev.crepe.domain.bank.exception.TokenAlreadyRequestedException;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.core.util.coin.global.repository.PortfolioRepository;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.coin.regulation.util.TokenCalculationUtil;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.model.entity.TokenPrice;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.repository.TokenPriceRepository;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenSetupService;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.TokenHistoryRepository;
import dev.crepe.domain.core.util.history.token.service.PortfolioHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankTokenSetupServiceImpl implements BankTokenSetupService {

    private final PortfolioHistoryService portfolioHistoryService;
    private final TokenPriceRepository tokenPriceRepository;
    private final BankTokenRepository bankTokenRepository;
    private final PortfolioRepository portfolioRepository;
    private final CoinRepository coinRepository;
    private final BankRepository bankRepository;
    private final TokenCalculationUtil tokenCalculationUtil;

    // 토큰 발행 프로세스
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BankToken requestTokenGenerate(CreateBankTokenRequest request, String bankEmail) {
        try {
            Bank bank = bankRepository.findByEmail(bankEmail)
                    .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 등록된 은행이 존재하지 않습니다."));

            if (bankTokenRepository.existsByBank_Id(bank.getId())) {
                throw new TokenAlreadyRequestedException("이미 발행 요청된 토큰이 존재합니다."); // RuntimeException 상속
            }

            BigDecimal totalSupplyAmount = tokenCalculationUtil.calculateTotalPrice(request);

            BankToken bankToken = BankToken.builder()
                    .bank(bank)
                    .name(request.getTokenName())
                    .currency(request.getTokenCurrency())
                    .totalSupply(totalSupplyAmount)
                    .status(BankTokenStatus.PENDING)
                    .build();
            bankTokenRepository.save(bankToken);

            TokenPrice tokenPrice = TokenPrice.builder()
                    .bankToken(bankToken)
                    .price(totalSupplyAmount)
                    .build();
            tokenPriceRepository.save(tokenPrice);

            request.getPortfolioCoins().forEach(coinInfo -> {
                Coin coin = coinRepository.findByCurrency(coinInfo.getCurrency());
                Portfolio portfolio = Portfolio.builder()
                        .bankToken(bankToken)
                        .coin(coin)
                        .amount(coinInfo.getAmount())
                        .initialPrice(coinInfo.getCurrentPrice())
                        .build();
                portfolioRepository.save(portfolio);
            });

            log.info("예상 총 발행량: {}", totalSupplyAmount);

            // 토큰 및 포토폴리오 내역 추가
            portfolioHistoryService.addTokenPortfolioHistory(request, bankToken, totalSupplyAmount);
            return bankToken;

        } catch (Exception e) {
            log.error("토큰 발행 요청 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 토큰 재발행 프로세스
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BankToken requestTokenReGenerate(ReCreateBankTokenRequest request, String bankEmail) {
        // 은행 이메일로 은행 정보 조회
        Bank bank = bankRepository.findByEmail(bankEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 등록된 은행이 존재하지 않습니다."));

        // 은행에 연결된 기존 토큰 조회
        BankToken bankToken = bankTokenRepository.findByBank(bank)
                .orElseThrow(() -> new IllegalArgumentException("해당 은행에 대한 토큰이 존재하지 않습니다."));

        // 유통 중인 토큰량 계산
        BigDecimal circulatingSupply = tokenCalculationUtil.getCirculatingSupply(bankToken);

        // 요청된 예상 토큰량 계산
        BigDecimal expectedTotal = tokenCalculationUtil.calculateTotalPrice(request);

        // 예상 토큰량 검증
        if (expectedTotal.compareTo(circulatingSupply.multiply(BigDecimal.valueOf(1.1))) < 0) {
            throw new IllegalArgumentException("예상 발행량이 유통 중인 토큰량보다 충분하지 않습니다. 포트폴리오를 재구성해야 합니다.");
        }

        // 토큰 변경 내역 업데이트
        portfolioHistoryService.updateTokenHistory(request, bankToken, expectedTotal);

        return bankToken;
    }



}