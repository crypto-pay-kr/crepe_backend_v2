package dev.crepe.domain.core.exchange.model.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;


@Getter
@Builder
@Setter
public class CreateExchangeRequest {

    private String fromCurrency;
    private String toCurrency;
    private Map<String, BigDecimal> coinRates;
    private BigDecimal tokenAmount;
    private BigDecimal coinAmount;
    private String traceId;

}
