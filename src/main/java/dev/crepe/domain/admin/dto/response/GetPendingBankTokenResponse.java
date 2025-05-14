package dev.crepe.domain.admin.dto.response;


import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetPendingBankTokenResponse {

    private String bankName;
    private LocalDateTime createdAt;
    private Text description;

    private String tokenName;
    private String tokenCurrency;
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
