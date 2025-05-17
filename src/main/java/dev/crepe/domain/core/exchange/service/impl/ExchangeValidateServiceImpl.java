package dev.crepe.domain.core.exchange.service.impl;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.exchange.exception.AmountNotAllowedException;
import dev.crepe.domain.core.exchange.exception.ExchangeValidationException;
import dev.crepe.domain.core.exchange.exception.TotalSupplyNotAllowedException;
import dev.crepe.domain.core.exchange.model.dto.request.CreateExchangeRequest;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeValidateServiceImpl {

    private final UpbitExchangeService upbitExchangeService;


    /**
     * 요청한 토큰양 검증
     */
    public BigDecimal validateRequestedTokenAmount(
            CreateExchangeRequest request,
            List<Portfolio> portfolios,
            BigDecimal totalSupply,
            List<Account> bankCoinAccounts
    ) {
        // 3. 시세 검증
        validateRates(request.getCoinRates());

        // 4. 자본금 계산
        BigDecimal totalCapital = calculateTotalCapitalKRW(portfolios, request.getCoinRates(),bankCoinAccounts);
        BigDecimal tokenPrice =calculateTokenPrice(totalCapital, totalSupply);

        // 5. 환전할 토큰 수량 계산
        BigDecimal tokenAmount = calculateAmount(
                request.getCoinAmount(),
                request.getCoinRates().get(request.getFromCurrency()),
                tokenPrice
        );

        // 6. 요청량 확인
        validateRequest(tokenAmount, request.getTokenAmount(),BigDecimal.valueOf(1));

        return tokenPrice;
    }


    /**
     * 요청한 코인 양 검증
     */
    public BigDecimal validateRequestedCoinAmount(
            CreateExchangeRequest request,
            List<Portfolio> portfolios,
            BigDecimal totalSupply,
            List<Account> bankCoinAccounts
    ) {
        // 1. 시세 검증
        validateRates(request.getCoinRates());

        // 2. 전체 자본금 계산
        BigDecimal totalCapital = calculateTotalCapitalKRW(portfolios, request.getCoinRates(), bankCoinAccounts);
        BigDecimal tokenPrice = calculateTokenPrice(totalCapital, totalSupply);

        // 3. 원화 → 환전 대상 코인 수량
        BigDecimal coinAmount = calculateAmount(
                request.getTokenAmount(),
                tokenPrice,
                request.getCoinRates().get(request.getToCurrency())

        );
        // 4. 요청량 확인
        validateRequest(coinAmount, request.getCoinAmount(), BigDecimal.valueOf(1));

        return tokenPrice;
    }


    /**
     * 실시간 시세와 포트폴리오를 기반으로 자본금(총 원화 가치)을 계산
     */
    public BigDecimal calculateTotalCapitalKRW(List<Portfolio> portfolios, Map<String, BigDecimal> coinRates, List<Account> bankCoinAccounts) {
        BigDecimal totalKRW = BigDecimal.ZERO;

        Map<String, BigDecimal> bankCoinMap = bankCoinAccounts.stream()
                .collect(Collectors.toMap(
                        acc -> acc.getCoin().getCurrency(),
                        Account::getBalance
                ));
        for (Portfolio p : portfolios) {
            String coin = p.getCoin().getCurrency();
            BigDecimal rate = coinRates.get(coin);
            BigDecimal bankBalance = bankCoinMap.getOrDefault(coin, BigDecimal.ZERO).max(BigDecimal.ZERO);
            BigDecimal value = p.getAmount().subtract(bankBalance).multiply(rate);
            totalKRW = totalKRW.add(value);
        }
        return totalKRW;
    }

    /**
     * 토큰 단가 = 총 자본금 / 현재 토큰양
     */
    public BigDecimal calculateTokenPrice(BigDecimal totalCapital, BigDecimal totalSupply) {
        if (totalSupply.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TotalSupplyNotAllowedException();
        }
        return totalCapital.divide(totalSupply, 8, RoundingMode.HALF_UP);
    }

    /**
     *환전 수량 계산
     */
    public BigDecimal calculateAmount(BigDecimal fromAmount, BigDecimal fromRate, BigDecimal coinRate) {
        if (fromAmount.compareTo(BigDecimal.ZERO) < 0 || fromRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new AmountNotAllowedException();
        }
        BigDecimal coinValue = fromAmount.multiply(fromRate);
        return coinValue.divide(coinRate, 8, RoundingMode.HALF_UP);
    }


    /**
     * 프론트에서 보낸 시세 확인
     */
    public void validateRates(Map<String, BigDecimal> clientRates) {
        for (Map.Entry<String, BigDecimal> entry : clientRates.entrySet()) {
            String currency = entry.getKey();
            BigDecimal rate = entry.getValue();

            if (rate == null) {
                throw new ExchangeValidationException("시세가 유효하지 않습니다");
            }

            upbitExchangeService.validateRateWithinThreshold(rate, currency,BigDecimal.valueOf(1));
        }
    }

    /**
     * 요청한 토큰 or 코인수량 확인
     */
    public void validateRequest(BigDecimal expected, BigDecimal actual, BigDecimal tolerancePercent) {
        if (expected.compareTo(BigDecimal.ZERO) == 0) {
            throw new ExchangeValidationException("expected 값은 0일 수 없습니다.");
        }

        BigDecimal diff = expected.subtract(actual).abs();
        BigDecimal percentDiff = diff.divide(expected, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (percentDiff.compareTo(tolerancePercent) > 0) {
            throw new ExchangeValidationException(
                    String.format("계산된 값 %.8f과 요청 값 %.8f의 오차율 %.4f%%가 허용 범위 %.2f%%를 초과했습니다.",
                            expected, actual, percentDiff, tolerancePercent));
        }

    }

}
