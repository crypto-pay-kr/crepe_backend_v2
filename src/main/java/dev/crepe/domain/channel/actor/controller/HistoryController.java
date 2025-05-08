package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.core.util.history.transfer.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.transfer.service.TransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@AllArgsConstructor
@Tag(name = "history API", description = "거래 내역 API")
public class HistoryController {

    private final TransactionHistoryService transactionHistoryService;

    @Operation(
            summary = " 내역 조회",
            description = "유저, 가맹점의 특정 코인 내역을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @GetMapping("/history")
    public ResponseEntity<List<GetTransactionHistoryResponse>> getSettlementHistory(
            AppAuthentication auth,
            @Parameter(description = "조회할 코인 단위", example = "XRP")
            @RequestParam("currency") String currency
    ) {
        //TODO 환전및 결제 내역도 추가
        List<GetTransactionHistoryResponse> response = transactionHistoryService.getSettlementHistory(auth.getUserEmail(), currency);
        return ResponseEntity.ok( response);
    }
}