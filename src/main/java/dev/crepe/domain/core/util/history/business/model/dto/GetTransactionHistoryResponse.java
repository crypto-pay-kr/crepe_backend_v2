package dev.crepe.domain.core.util.history.business.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Builder
public class GetTransactionHistoryResponse {

    private String status;
    private BigDecimal amount;
    private String type;
    private LocalDateTime transferredAt;
    private BigDecimal afterBalance;
    //송금 시 받거나 주는 사람 이름
    private String name;
}
