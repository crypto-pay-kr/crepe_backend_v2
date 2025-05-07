package dev.crepe.domain.channel.actor.user.controller;

import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.service.WithdrawService;
import dev.crepe.domain.store.model.dto.request.GetStoreSettlementRequest;
import dev.crepe.domain.store.model.dto.response.GetSettlementHistoryResponse;
import dev.crepe.domain.store.service.RequestSettlementService;
import dev.crepe.domain.store.service.SettlementHistoryService;
import dev.crepe.global.auth.jwt.AppAuthentication;
import dev.crepe.global.auth.role.SellerAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
@Tag(name = "User Withdraw API", description = "유저 출금 관련 API")
public class WithdrawController {

    private final WithdrawService withdrawService;

    @Operation(
            summary = "정산 요청",
            description = "유저가 출금(정산)을 요청합니다. ",
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