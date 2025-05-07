package dev.crepe.domain.channel.actor.store.controller;

import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.service.WithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
@AllArgsConstructor
@Tag(name = "User Withdraw API", description = "유저 출금 관련 API")
public class WithdrawController {

    private final WithdrawService withdrawService;

    @Operation(
            summary = "정산 요청",
            description = "가맹점이 출금(정산)을 요청합니다. 정산 요청은 처리 대기 상태로 저장되며, 별도 스케줄러에 의해 확인됩니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @UserAuth
    @PostMapping("/withdraw")
    public ResponseEntity<String> requestWithdraw(
            AppAuthentication auth,
            @RequestBody GetWithdrawRequest request
    ) {
        withdrawService.requestWithdraw(request, auth.getUserEmail());
        return ResponseEntity.ok("정산요청 완료");
    }

}