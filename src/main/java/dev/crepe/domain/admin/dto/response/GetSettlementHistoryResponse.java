package dev.crepe.domain.admin.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GetSettlementHistoryResponse {

    private Long id;
    private LocalDateTime date;
    private String status;
    private BigDecimal amount;
    private String currency;
}
