package dev.crepe.domain.admin.dto.response;


import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllBankTokenResponse {


    private String bankName;
    private LocalDateTime createdAt;
    private String description;

    private Long bankTokenId;
    private String tokenName;
    private String tokenCurrency;
    private BankTokenStatus status;
    private BigDecimal totalSupply;
    private List<CoinInfo> portfolioCoins;


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoinInfo {
        private String coinName;
        private BigDecimal amount;
        private String currency;
        private BigDecimal currentPrice;
    }
}
