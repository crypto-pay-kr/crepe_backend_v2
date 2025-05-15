package dev.crepe.domain.core.util.coin.regulation.util;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
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
}