package dev.crepe.domain.core.util.coin.regulation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crepe.domain.core.util.coin.regulation.model.Currency;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.model.entity.TokenPrice;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.repository.TokenPriceRepository;
import dev.crepe.domain.core.util.coin.regulation.service.TokenPriceService;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenPriceServiceImpl implements TokenPriceService {

    private final TokenPriceRepository tokenPriceRepository;
    private final BankTokenRepository bankTokenRepository;
    private final UpbitExchangeService upbitExchangeService;
    private final ObjectMapper objectMapper;

    @Override
    public void createAndSaveTokenPrice(BankToken bankToken, BigDecimal price) {
        TokenPrice tokenPrice = TokenPrice.builder()
                .bankToken(bankToken)
                .price(price)
                .build();
        tokenPriceRepository.save(tokenPrice);
    }

    @Override
    @Transactional
    public void saveTokenPrice() {
        List<BankToken> bankTokens = bankTokenRepository.findAll();

        for (BankToken bankToken : bankTokens) {
            try {
                BigDecimal totalPrice = BigDecimal.ZERO;
                Map<String, String> priceDetailsMap = new HashMap<>();

                // Portfolio에서 Currency와 Amount 가져오기
                List<Portfolio> portfolios = bankToken.getPortfolios();
                for (Portfolio portfolio : portfolios) {
                    String currency = portfolio.getCoin().getCurrency();
                    BigDecimal amount = portfolio.getAmount();

                    // upbitexchange로 시세 조회
                    BigDecimal latestRate = upbitExchangeService.getLatestRate(currency);

                    // 총 가격 계산
                    totalPrice = totalPrice.add(
                            latestRate.multiply(amount).setScale(8, RoundingMode.HALF_UP)
                    );
                    // priceDetails에 추가
                    priceDetailsMap.put("[" + currency + "]", latestRate.toPlainString());
                }
                // JSON 변환
                String priceDetailsJson = objectMapper.writeValueAsString(priceDetailsMap);

                // 이전 TokenPrice 조회
                BigDecimal previousTotalPrice = getPreviousTotalPrice(bankToken);

                // 등락율 계산
                BigDecimal changeRate = calculateChangeRate(previousTotalPrice, totalPrice);

                // TokenPrice 저장
                TokenPrice tokenPrice = TokenPrice.builder()
                        .bankToken(bankToken)
                        .price(totalPrice)
                        .priceDetails(priceDetailsJson)
                        .changeRate(changeRate != null ? changeRate.toPlainString() : null)
                        .build();

                tokenPriceRepository.save(tokenPrice);
                log.info("{} 토큰 시세 저장 완료: {} - 가격: {}, 등락율: {}", bankToken.getName(), bankToken.getCurrency(), totalPrice, changeRate);
            } catch (Exception e) {
                log.error("{} 토큰 시세 저장 실패: {} - 에러: {}", bankToken.getName(), bankToken.getCurrency(), e.getMessage());
            }
        }

    }

        private BigDecimal getPreviousTotalPrice(BankToken bankToken) {
            // BankToken에 대한 가장 최근 TokenPrice 조회
            return tokenPriceRepository.findTopByBankTokenOrderByCreatedAtDesc(bankToken)
                    .map(TokenPrice::getPrice)
                    .orElse(null);
        }

        private BigDecimal calculateChangeRate(BigDecimal previousPrice, BigDecimal currentPrice) {
            if (previousPrice == null || previousPrice.compareTo(BigDecimal.ZERO) == 0) {
                return null; // 이전 가격이 없거나 0이면 등락율 계산 불가
            }
            return currentPrice.subtract(previousPrice)
                    .divide(previousPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }


}