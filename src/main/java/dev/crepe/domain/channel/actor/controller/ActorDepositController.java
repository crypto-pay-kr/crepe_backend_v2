package dev.crepe.domain.channel.actor.controller;


import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.service.ActorDepositService;
import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import dev.crepe.domain.core.transfer.service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deposit API", description = "입금 API")
public class ActorDepositController {

    private final ActorDepositService actorDepositService;

    @Operation(
            summary = "계좌 입금 요청",
            description = "현재 로그인한 유저, 가맹점 계좌에 입금을 요청합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @PostMapping("/deposit")
    public ResponseEntity<String> requestDeposit(@RequestBody GetDepositRequest request,
                                                 AppAuthentication auth) {
        actorDepositService.requestDeposit(request, auth.getUserEmail());
        return ResponseEntity.ok("입금 처리가 완료되었습니다.");
    }

}

