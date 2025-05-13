package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.service.ActorWithdrawService;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.service.WithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@AllArgsConstructor
@Tag(name = "Withdraw API", description = "출금 관련 API")
public class ActorWithdrawController {

    private final ActorWithdrawService actorWithdrawService;

    @Operation(
            summary = "정산 요청",
            description = "유저, 가맹점이 출금(정산)을 요청합니다. 정산 요청은 처리 대기 상태로 저장되며, 별도 스케줄러에 의해 확인됩니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @PostMapping("/withdraw")
    public ResponseEntity<String> requestWithdraw(
            AppAuthentication auth,
            @RequestBody GetWithdrawRequest request
    ) {
        actorWithdrawService.requestWithdraw(request, auth.getUserEmail());
        return ResponseEntity.ok("정산요청 완료");
    }

}