package dev.crepe.domain.core.account.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "입금 주소 등록 응답 DTO")
public class GetAddressResponse {
    private String currency;
    private String address;
    private String tag;
    private String addressRegistryStatus;
}
