package dev.crepe.domain.core.util.history.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CoinUsageDto {
    private String currency;
    private BigDecimal usageAmount;
}
