package dev.crepe.domain.core.transfer.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Schema(description = "출금 응답 확인 DTO")
public class CheckWithdrawResponse {
    private boolean completed;
    private BigDecimal amount;
    private String doneAt;
}