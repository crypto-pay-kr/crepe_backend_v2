package dev.crepe.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "계좌 주소 요청 거절 DTO")
public class RejectAddressRequest {

    @Schema(description = "거절 사유")
    private String rejectReason;
}
