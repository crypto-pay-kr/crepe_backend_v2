package dev.crepe.domain.core.util.coin.regulation.util;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TokenCalculationUtil {

    public BigDecimal calculateTotalPrice(CreateBankTokenRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (CreateBankTokenRequest.CoinInfo coin : request.getPortfolioCoins()) {
            if (coin.getAmount() != null && coin.getCurrentPrice() != null) {
                total = total.add(coin.getAmount().multiply(coin.getCurrentPrice()));
            }
        }
        return total;
    }

    public BigDecimal calculateTotalPrice(ReCreateBankTokenRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (ReCreateBankTokenRequest.CoinInfo coin : request.getPortfolioCoins()) {
            if (coin.getAmount() != null && coin.getCurrentPrice() != null) {
                total = total.add(coin.getAmount().multiply(coin.getCurrentPrice()));
            }
        }
        return total;
    }


    // 유통중인 토큰량 계산
    public BigDecimal getCirculatingSupply(BankToken bankToken) {
        // BankToken과 연결된 모든 계좌의 잔액 합계를 계산
        return bankToken.getPortfolios().stream()
                .map(portfolio -> portfolio.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}