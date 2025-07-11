
package dev.crepe.domain.bank.model.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "은행 토큰 재생성 요청 DTO")
public class ReCreateBankTokenRequest {

    @Schema(description = "변경 사유", example = "변경 사유")
    private String changeReason;
    @ArraySchema(schema = @Schema(description = "포트폴리오 코인 정보 리스트"))
    private List<CoinInfo> portfolioCoins;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "포트폴리오 코인 정보")
    public static class CoinInfo {

        @Schema(description = "코인 이름", example = "XRP")
        private String coinName;
        @Schema(description = "코인 수량", example = "10.5")
        private BigDecimal amount;
        @Schema(description = "코인 심볼", example = "5000")
        private String currency;
        @Schema(description = "현재 가격", example = "50000")
        private BigDecimal currentPrice;

    }


}
