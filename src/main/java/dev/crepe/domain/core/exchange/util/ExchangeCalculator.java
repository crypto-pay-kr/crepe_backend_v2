package dev.crepe.domain.core.exchange.util;

import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExchangeCalculator {

    /**
     * 실시간 시세와 포트폴리오를 기반으로 자본금(총 원화 가치)을 계산
     */
    public BigDecimal calculateTotalCapitalKRW(List<Portfolio> portfolios, Map<String, BigDecimal> coinRates) {
        BigDecimal totalKRW = BigDecimal.ZERO;
        for (Portfolio p : portfolios) {
            String coin = p.getCoin().getCurrency();
            BigDecimal rate = coinRates.get(coin);
            BigDecimal value = p.getAmount().multiply(rate);
            totalKRW = totalKRW.add(value);
        }
        return totalKRW;
    }
    /**
     * 토큰 단가 = 총 자본금 / 총 발행량
     */
    public BigDecimal calculateTokenPrice(BigDecimal totalCapital, BigDecimal totalSupply) {
        return totalCapital.divide(totalSupply, 8, RoundingMode.HALF_UP);
    }

    /**
     * 코인 -> 토큰 환전 수량 계산
     */
    public BigDecimal calculateTokenAmount(BigDecimal coinAmount, BigDecimal coinRate, BigDecimal tokenPrice) {
        BigDecimal coinValue = coinAmount.multiply(coinRate);
        return coinValue.divide(tokenPrice, 8, RoundingMode.HALF_UP);
    }

    /**
     * 토큰 → 코인 환전 수량 계산 (자본금 비율 고려)
     */
    public BigDecimal calculateCoinAmount(
            BigDecimal tokenKRW,
            BigDecimal coinValueInCapital, // 포트폴리오에서 해당 코인이 차지하는 원화 가치
            BigDecimal totalCapitalKRW,
            BigDecimal coinRate
    ) {
        // 전체 자본금에서 이 코인이 차지하는 비율
        BigDecimal coinRatio = coinValueInCapital.divide(totalCapitalKRW, 8, RoundingMode.HALF_UP);

        // 내가 환전하는 토큰 원화 가치 중에서, 이 코인으로 환전 가능한 원화 한도
        BigDecimal allowedKRW = tokenKRW.multiply(coinRatio);

        // 해당 원화로 환전 가능한 코인 수량
        return allowedKRW.divide(coinRate, 8, RoundingMode.HALF_UP);
    }
}
