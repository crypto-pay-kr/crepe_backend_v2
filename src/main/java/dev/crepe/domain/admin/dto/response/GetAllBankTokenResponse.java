package dev.crepe.domain.admin.dto.response;


import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.history.token.model.TokenRequestType;
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


    private Long bankId;
    private String bankName;

    private Long bankTokenId;
    private Long tokenHistoryId;

    private String changeReason;
    private String rejectReason;

    private TokenRequestType requestType;
    private BankTokenStatus status;
    private LocalDateTime createdAt;

    private BigDecimal totalSupplyAmount;
    private List<PortfolioDetail> portfolioDetails;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioDetail {
        private String coinName;
        private String coinCurrency;
        private BigDecimal prevAmount;
        private BigDecimal prevPrice;
        private BigDecimal updateAmount;
        private BigDecimal updatePrice;
    }
}
