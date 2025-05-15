
package dev.crepe.domain.core.exchange.util;

import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ExchangeValidator {

    public void validateRates(Map<String, BigDecimal> clientRates, UpbitExchangeService upbitExchangeService) {
        for (Map.Entry<String, BigDecimal> entry : clientRates.entrySet()) {
            String currency = entry.getKey();
            BigDecimal rate = entry.getValue();

            if (rate == null) {
                throw new IllegalArgumentException(currency + "의 시세가 누락되었습니다.");
            }

            upbitExchangeService.validateExchangeRate(rate, currency);
        }
    }

    public void assertEquals(BigDecimal expected, BigDecimal actual) {
        if (expected.compareTo(actual) != 0) {
            throw new IllegalArgumentException("errorMessage");
        }
    }
}