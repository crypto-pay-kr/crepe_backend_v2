package dev.crepe.domain.bank.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "은행 계좌 정보 응답 DTO")
public class GetCoinAccountInfoResponse {


    private String bankname;
    private String coinName;
    private String currency;
    private String accountAddress;
    private String tag;
    private String balance;


}
