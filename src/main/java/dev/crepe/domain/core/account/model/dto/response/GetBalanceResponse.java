package dev.crepe.domain.core.account.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "잔액 응답 DTO")
public class GetBalanceResponse {

    private String coinImageUrl;
    private String coinName;
    private String currency;
    private BigDecimal balance;
}
