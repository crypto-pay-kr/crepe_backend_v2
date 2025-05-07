package dev.crepe.domain.channel.actor.store.controller;

import dev.crepe.domain.core.util.history.transfer.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.transfer.service.TransactionHistoryService;
import dev.crepe.domain.store.model.dto.response.GetSettlementHistoryResponse;
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
@RequestMapping("/api/store")
@AllArgsConstructor
@Tag(name = "store Withdraw API", description = "가맹점 출금 관련 API")
public class HistoryController {

    private final TransactionHistoryService transactionHistoryService;

    @Operation(
            summary = " 내역 조회",
            description = "가맹점의 특정 코인 내역을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @SellerAuth
    @GetMapping("/history")
    public ResponseEntity<List<GetTransactionHistoryResponse>> getSettlementHistory(
            AppAuthentication auth,
            @Parameter(description = "조회할 코인 단위", example = "XRP")
            @RequestParam("currency") String currency
    ) {
        List<GetTransactionHistoryResponse> response = transactionHistoryService.getSettlementHistory(auth.getUserEmail(), currency);
        return ResponseEntity.ok( response);
    }
}