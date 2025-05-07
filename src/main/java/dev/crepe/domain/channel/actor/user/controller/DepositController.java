package dev.crepe.domain.channel.actor.user.controller;


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
@RequestMapping("/api/user/deposit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "user Deposit API", description = "입금 API")
public class DepositController {

    private final DepositService depositService;

    @Operation(
            summary = "사용자 계좌 입금 요청",
            description = "현재 로그인한 사용자의 계좌에 입금을 요청합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @UserAuth
    @PostMapping
    public ResponseEntity<String> requestDeposit(@RequestBody GetDepositRequest request,
                                                 AppAuthentication auth) {
        depositService.requestDeposit(request, auth);
        return ResponseEntity.ok("입금 처리가 완료되었습니다.");
    }
}

