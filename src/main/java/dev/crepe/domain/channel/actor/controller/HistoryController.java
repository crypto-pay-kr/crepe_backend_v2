package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import dev.crepe.domain.core.util.history.business.service.TransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<Slice<GetTransactionHistoryResponse>> getHistory(
            AppAuthentication auth,
            @RequestParam("currency") String currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        //TODO 환전내역 추가
        Slice<GetTransactionHistoryResponse> response = transactionHistoryService.getTransactionHistory(
                auth.getUserEmail(), currency, page, size);
        return ResponseEntity.ok(response);
    }
}