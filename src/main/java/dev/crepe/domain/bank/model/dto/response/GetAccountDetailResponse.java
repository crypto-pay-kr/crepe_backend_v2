package dev.crepe.domain.bank.model.dto.response;

import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "은행 계좌 상세 정보 응답 DTO")
public class GetAccountDetailResponse {

    private String bankName;
    private GetAddressResponse addressResponse;
}
