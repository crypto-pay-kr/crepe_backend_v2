package dev.crepe.domain.core.exchange.model.dto;

import lombok.Getter;
import java.math.BigDecimal;
import java.util.Map;


@Getter
public class GetExchangeRequest {

    private String fromCurrency;
    private String toCurrency;
    private Map<String, BigDecimal> CoinRates;
    private BigDecimal tokenAmount;
    private BigDecimal coinAmount;

}
