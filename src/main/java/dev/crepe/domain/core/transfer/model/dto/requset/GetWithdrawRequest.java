package dev.crepe.domain.core.transfer.model.dto.requset;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "출금 요청 DTO")
public class GetWithdrawRequest {

    private String currency;
    private String amount;
    private String traceId;

}