package dev.crepe.domain.bank.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "담당자 번호 변경 요청 DTO")
public class ChangeBankPhoneRequest {
    @Schema(description = "새 담당자 연결 번호", example = "0321231234")
    private String bankPhoneNumber;
}
