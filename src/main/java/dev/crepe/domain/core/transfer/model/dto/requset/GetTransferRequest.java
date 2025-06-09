package dev.crepe.domain.core.transfer.model.dto.requset;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "송금 요청 DTO")
public class GetTransferRequest {

    private String receiverEmail;
    private String currency;
    private BigDecimal amount;
    private String traceId;

}
