package dev.crepe.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "은행 토큰 발행 요청 반려 DTO")
public class RejectBankTokenRequest {

    @Schema(description = "반려 이유")
    private String rejectReason; // ex. 반려 이유
}
