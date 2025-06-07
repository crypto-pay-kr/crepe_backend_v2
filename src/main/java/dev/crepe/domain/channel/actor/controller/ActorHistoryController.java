package dev.crepe.domain.channel.actor.controller;

import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.service.ActorHistoryService;
import dev.crepe.domain.core.util.history.business.model.dto.GetTransactionHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;


@RestController
@RequestMapping("/history")
@AllArgsConstructor
@Tag(name = "history API", description = "거래 및 환전 내역 API")
public class ActorHistoryController {

    private final ActorHistoryService actorHistoryService;

    @Operation(
            summary = "거래내역 조회",
            description = "유저, 가맹점의 특정 코인 거래내역을 조회합니다.",
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
        long startTime = System.currentTimeMillis();

        Slice<GetTransactionHistoryResponse> response = actorHistoryService.getNonRegulationHistory(
                auth.getUserEmail(), currency, page, size);

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // 응답 시간과 캐시 정보를 헤더에 추가
        return ResponseEntity.ok()
                .header("X-Response-Time", String.valueOf(responseTime))
                .header("X-Cache-Enabled", "true")
                .header("X-Total-Elements", String.valueOf(response.getContent().size()))
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30)))
                .body(response);
    }



    @Operation(
            summary = " 환전 내역 조회",
            description = "유저, 가맹점의 환전 내역을 조회합니다.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @GetMapping("/token")
    public ResponseEntity<Slice<GetTransactionHistoryResponse>> getExchangeHistory(
            AppAuthentication auth,
            @RequestParam("currency") String currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Slice<GetTransactionHistoryResponse> response = actorHistoryService.getTokenHistory(
                auth.getUserEmail(), currency, page, size
        );
        return ResponseEntity.ok(response);
    }


}