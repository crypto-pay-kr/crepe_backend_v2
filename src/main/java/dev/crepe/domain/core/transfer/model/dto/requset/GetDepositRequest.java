package dev.crepe.domain.core.transfer.model.dto.requset;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "입금 요청 DTO")
public class GetDepositRequest {

    private String txid;
    private String currency;
    private String traceId;

}
