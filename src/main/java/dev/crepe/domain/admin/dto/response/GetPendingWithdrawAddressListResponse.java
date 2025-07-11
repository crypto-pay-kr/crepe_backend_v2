package dev.crepe.domain.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "관리자 - 요청받은 응답 리스트 DTO ")
public class GetPendingWithdrawAddressListResponse {
    private Long id;
    private String depositor;
    private String userType;
    private String currency;
    private String address;
    private String tag;
    private String addressRegistryStatus;
    private LocalDateTime createdAt;

}