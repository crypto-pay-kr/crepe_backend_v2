package dev.crepe.domain.core.util.history.pay.controller;

import dev.crepe.domain.admin.dto.response.GetPayHistoryResponse;
import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.core.pay.service.PayService;
import dev.crepe.domain.core.util.history.pay.service.PayHistoryReaderService;
import dev.crepe.domain.core.util.history.pay.service.PayHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayHistoryController {

    private final PayHistoryReaderService payHistoryReaderService;

    @Operation(
            summary = "유저 결제내역 조회",
            description = "유저 결제 내역 조회",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @UserAuth
    @GetMapping("")
    public ResponseEntity<Page<GetPayHistoryResponse>> getUserPayHistories(
            AppAuthentication auth,
            @RequestParam(required = false) String type,
            Pageable pageable
    ) {
        Page<GetPayHistoryResponse> result = payHistoryReaderService.getPayHistoriesByUserEmail(auth.getUserEmail(), type, pageable);
        return ResponseEntity.ok(result);
    }
}
