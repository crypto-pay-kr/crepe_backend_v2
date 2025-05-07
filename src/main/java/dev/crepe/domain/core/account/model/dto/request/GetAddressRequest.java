package dev.crepe.domain.core.account.model.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "계좌 조회 DTO")
public class GetAddressRequest {
    private String currency;
    private String address;
    private String tag;
}