package dev.crepe.domain.admin.dto.request;

import dev.crepe.domain.bank.model.entity.BankStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "은행 계정 정지, 해제 DTO")
public class ChangeBankStatusRequest {
    private Long bankId;
    private BankStatus bankStatus;
}
