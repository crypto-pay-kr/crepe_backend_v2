package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.channel.actor.service.ActorTransferService;
import dev.crepe.domain.core.deposit.model.dto.request.TokenDepositRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetTransferRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Slf4j
@AllArgsConstructor
@Tag(name = "Withdraw API", description = "이체 관련 API")
public class ActorTransferController {

    private final ActorTransferService actorTransferService;

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
        actorTransferService.requestWithdraw(request, auth.getUserEmail(), request.getTraceId());
        return ResponseEntity.ok("정산요청 완료");
    }


    @Operation(
            summary = "송금 요청",
            description = "유저, 가맹점이 송금을 요청합니다",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @PostMapping("/transfer")
    public ResponseEntity<String> requestTransfer(
            AppAuthentication auth,
            @RequestBody GetTransferRequest request
    ) {
        actorTransferService.requestTransfer(request, auth.getUserEmail(), request.getTraceId());
        return ResponseEntity.ok("송금요청 완료");
    }


    @Operation(
            summary = "송금 요청",
            description = "계좌를 조회합니다" ,
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @GetMapping("/receiver-name")
    public ResponseEntity<String> getReceiverName(@RequestParam String email,
                                                  @RequestParam String currency,
       AppAuthentication auth) {
        String name = actorTransferService.getAccountHolderName(email,auth.getUserEmail(), currency);
        return ResponseEntity.ok(name);
    }



    @Operation(
            summary = "계좌 입금 요청",
            description = "현재 로그인한 유저, 가맹점 계좌에 입금을 요청합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @PostMapping("/deposit")
    public ResponseEntity<String> requestDeposit(@RequestBody GetDepositRequest request,
                                                 AppAuthentication auth) {
        actorTransferService.requestDeposit(request, auth.getUserEmail(), request.getTraceId());
        return ResponseEntity.ok("입금 처리가 완료되었습니다.");
    }



    @PostMapping("/deposit/token")
    @UserAuth
    @Operation(summary = "토큰 예치", description = "상품에 은행 토큰을 예치합니다.")
    public ResponseEntity<?> depositToProduct(@RequestBody TokenDepositRequest request,
                                              AppAuthentication auth) {
        String result = actorTransferService.requestTokenDeposit(auth.getUserEmail(),
                request.getSubscribeId(), request.getAmount(),request.getTraceId());
        return ResponseEntity.ok(result);
    }

}