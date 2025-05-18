package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.service.ActorHistoryService;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/history")
@AllArgsConstructor
@Tag(name = "history API", description = "거래 내역 API")
public class ActorHistoryController {

    private final ActorHistoryService actorHistoryService;

    @Operation(
            summary = " 내역 조회",
            description = "유저, 가맹점의 특정 코인 내역을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @GetMapping("/coin")
    public ResponseEntity<Slice<GetTransactionHistoryResponse>> getHistory(
            AppAuthentication auth,
            @RequestParam("currency") String currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Slice<GetTransactionHistoryResponse> response = actorHistoryService.getNonRegulationHistory(
                auth.getUserEmail(), currency, page, size);
        return ResponseEntity.ok(response);
    }
}