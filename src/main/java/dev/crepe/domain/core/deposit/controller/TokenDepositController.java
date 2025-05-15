package dev.crepe.domain.core.deposit.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.core.deposit.model.dto.request.TokenDepositRequest;
import dev.crepe.domain.core.deposit.service.TokenDepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deposit/token")
@RequiredArgsConstructor
@Tag(name = "TokenDeposit API", description = "토큰 예치 API")
public class TokenDepositController {

    private final TokenDepositService tokenDepositService;

    @PostMapping
    @UserAuth
    @Operation(summary = "토큰 예치", description = "상품에 은행 토큰을 예치합니다.")
    public ResponseEntity<?> depositToProduct(@RequestBody TokenDepositRequest request, AppAuthentication auth) {
        String result = tokenDepositService.depositToProduct(auth.getUserEmail(),request.getSubscribeId(), request.getAmount());
        return ResponseEntity.ok(result);
    }
}
