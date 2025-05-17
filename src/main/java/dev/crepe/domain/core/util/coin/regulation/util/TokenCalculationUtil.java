package dev.crepe.domain.core.util.coin.regulation.util;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.coin.regulation.exception.InvalidTokenGenerateException;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Component
public class TokenCalculationUtil {


    // CreateBankTokenRequest를 기반으로 총 발행량 계산
    public BigDecimal calculateTotalPrice(CreateBankTokenRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (CreateBankTokenRequest.CoinInfo coin : request.getPortfolioCoins()) {
            if (coin.getAmount() != null && coin.getCurrentPrice() != null) {
                total = total.add(coin.getAmount().multiply(coin.getCurrentPrice()));
            }
        }
        return total;
    }


    // ReCreateBankTokenRequest를 기반으로 총 발행량 계산
    public BigDecimal calculateTotalPrice(ReCreateBankTokenRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (ReCreateBankTokenRequest.CoinInfo coin : request.getPortfolioCoins()) {
            if (coin.getAmount() != null && coin.getCurrentPrice() != null) {
                total = total.add(coin.getAmount().multiply(coin.getCurrentPrice()));
            }
        }
        return total;
    }


    // 유통 중인 토큰량 계산
    public BigDecimal getCirculatingSupply(Account bankTokenAccount) {
        return bankTokenAccount.getBalance().subtract(bankTokenAccount.getAvailableBalance());
    }


    // 기존 포트폴리오의 각 코인별 총 가치 계산
    private Map<String, BigDecimal> getExistingCoinValues(BankToken bankToken) {
        Map<String, BigDecimal> coinValues = new HashMap<>();
        for (Portfolio portfolio : bankToken.getPortfolios()) {
            String coinName = portfolio.getCoin().getName();
            BigDecimal totalValue = portfolio.getAmount().multiply(portfolio.getInitialPrice());
            coinValues.put(coinName, coinValues.getOrDefault(coinName, BigDecimal.ZERO).add(totalValue));
        }
        return coinValues;
    }

    // 기존 토큰의 총 가치 계산
    private BigDecimal getOriginalTotalValue(BankToken bankToken) {
        return bankToken.getPortfolios().stream()
                .map(portfolio -> portfolio.getAmount().multiply(portfolio.getInitialPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // 기존 코인들의 가치 비율을 기준으로 새 포트폴리오 검증
    public void validatePortfolioChange(
            ReCreateBankTokenRequest request,
            BankToken existingToken,
            BigDecimal circulatingSupply) {
        // 기존 포트폴리오의 초기 가치 계산
        BigDecimal originalTotalValue = getOriginalTotalValue(existingToken);

        // 기존 포트폴리오의 각 코인별 가치 비율 계산
        Map<String, BigDecimal> coinValueRatios = new HashMap<>();
        for (Portfolio portfolio : existingToken.getPortfolios()) {
            String coinName = portfolio.getCoin().getName();
            BigDecimal coinValue = portfolio.getAmount().multiply(portfolio.getInitialPrice());
            BigDecimal ratio = coinValue.divide(originalTotalValue, 10, RoundingMode.HALF_UP);
            coinValueRatios.put(coinName, coinValueRatios.getOrDefault(coinName, BigDecimal.ZERO).add(ratio));
        }

        // 현재 유통 중인 토큰 기준으로 각 코인별 가치 계산
        Map<String, BigDecimal> circulatingCoinValues = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : coinValueRatios.entrySet()) {
            String coinName = entry.getKey();
            BigDecimal ratio = entry.getValue();
            BigDecimal circulatingValue = circulatingSupply.multiply(ratio);
            circulatingCoinValues.put(coinName, circulatingValue);
        }

        // 새 포트폴리오에 포함된 코인들의 이름과 가치를 맵으로 변환
        Map<String, BigDecimal> newCoinValues = new HashMap<>();
        for (ReCreateBankTokenRequest.CoinInfo coin : request.getPortfolioCoins()) {
            String coinName = coin.getCoinName();
            BigDecimal coinValue = coin.getAmount().multiply(coin.getCurrentPrice());
            newCoinValues.put(coinName, newCoinValues.getOrDefault(coinName, BigDecimal.ZERO).add(coinValue));
        }

        // 새 포트폴리오에 포함된 코인 이름 목록
        Set<String> newCoinNames = newCoinValues.keySet();

        // 1. 현재 유통 중인 토큰의 코인별 가치를 기준으로 검증
        for (Map.Entry<String, BigDecimal> entry : circulatingCoinValues.entrySet()) {
            String coinName = entry.getKey();
            BigDecimal circulatingValue = entry.getValue();

            // 기존 코인이 새 포트폴리오에도 포함되어 있는지 확인
            if (newCoinNames.contains(coinName)) {
                BigDecimal newValue = newCoinValues.get(coinName);

                // 같은 코인의 가치가 유통 중인 가치보다 작은지 확인
                if (newValue.compareTo(circulatingValue) < 0) {
                    throw new InvalidTokenGenerateException(
                            "코인 '" + coinName + "'의 가치가 부족합니다. 현재 유통 중인 가치("
                                    + circulatingValue + ")보다 적어도 같거나 더 많은 가치("
                                    + newValue + ")가 필요합니다.");
                }
            } else {
                // 기존 코인이 새 포트폴리오에서 완전히 제거된 경우
                throw new InvalidTokenGenerateException(
                        "코인 '" + coinName + "'이(가) 포트폴리오에서 제거되었습니다. 현재 유통 중인 가치("
                                + circulatingValue + ") 이상의 동일 코인이 반드시 포함되어야 합니다.");
            }
        }

        // 2. 총 가치 검증 (추가 안전성 검증)
        BigDecimal newTotalValue = calculateTotalPrice(request);
        if (newTotalValue.compareTo(circulatingSupply) < 0) {
            throw new InvalidTokenGenerateException(
                    "새 포트폴리오의 총 가치(" + newTotalValue + ")가 현재 유통 중인 토큰량("
                            + circulatingSupply + ")보다 작습니다.");
        }
    }


    // 변경 포토폴리오의 총 가지가 유통중인 토큰량의 안전계수(1.1)보다 큰지 확인
    public void validatePortfolioSafety(
            ReCreateBankTokenRequest request,
            BigDecimal circulatingSupply,
            BigDecimal safetyFactor)
    {
        // 새 포트폴리오의 예상 총 가치 계산
        BigDecimal expectedTotal = calculateTotalPrice(request);

        // 예상 총 가치가 유통량에 안전계수를 곱한 값보다 작으면 안전하지 않음
        if (expectedTotal.compareTo(circulatingSupply.multiply(safetyFactor)) < 0) {
            throw new InvalidTokenGenerateException(
                    "예상 발행량(" + expectedTotal + ")이 유통 중인 토큰량(" +
                            circulatingSupply.multiply(safetyFactor) + ")보다 부족합니다. 포트폴리오를 재구성해야 합니다.");
        }
    }
}