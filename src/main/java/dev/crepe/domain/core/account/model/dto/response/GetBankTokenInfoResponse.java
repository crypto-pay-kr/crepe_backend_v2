package dev.crepe.domain.core.account.model.dto.response;


import dev.crepe.domain.core.product.model.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "은행 토크 조회")
public class GetBankTokenInfoResponse {

    private String bankImageUrl;
    private String currency;
    private String name;
    private BigDecimal balance;
    private List<GetProductResponse> product;


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetProductResponse {
        private Long subscribeId;
        private String name;
        private BigDecimal balance;
        private String imageUrl;
        }

}
