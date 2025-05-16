package dev.crepe.domain.core.util.coin.regulation.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenInfoResponse {
    private String Currency;
    private BigDecimal totalSupply;
    private BigDecimal tokenBalance;
    private List<PortfolioItem> portfolios;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PortfolioItem {
        private String currency;
        private BigDecimal amount;
        private BigDecimal nonAvailableAmount;

    }

}
