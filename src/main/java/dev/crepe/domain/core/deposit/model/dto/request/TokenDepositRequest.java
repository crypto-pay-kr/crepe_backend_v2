package dev.crepe.domain.core.deposit.model.dto.request;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TokenDepositRequest {
    private Long subscribeId;
    private BigDecimal amount;
    private String traceId;
}