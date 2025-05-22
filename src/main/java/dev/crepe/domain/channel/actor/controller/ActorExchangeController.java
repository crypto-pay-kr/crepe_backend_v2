package dev.crepe.domain.channel.actor.controller;


import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.channel.actor.service.ActorExchangeService;
import dev.crepe.domain.core.exchange.model.dto.request.CreateExchangeRequest;
import dev.crepe.domain.core.util.coin.regulation.model.dto.request.TokenInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "exchange API", description = "exchange API")
public class ActorExchangeController {

    private final ActorExchangeService actorExchangeService;

    @Operation(
            summary = "코인-> 토큰 환전 요청  ",
            description = "토큰 환전요청을 한다",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @PostMapping("/token")
    public ResponseEntity<String> requestExchangeToToken(@RequestBody CreateExchangeRequest request,
                                                 AppAuthentication auth) {
        actorExchangeService.requestExchangeToToken(auth.getUserEmail(), request );
        return ResponseEntity.ok("코인에서 토큰으로 환전 요청이 완료되었습니다");
    }

    @Operation(
            summary = "토큰 -> 코인 환전 요청  ",
            description = "코인 환전요청을 한다",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @PostMapping("/coin")
    public ResponseEntity<String> requestExchangeToCoin(@RequestBody CreateExchangeRequest request,
                                                 AppAuthentication auth) {
        actorExchangeService.requestExchangeToCoin(auth.getUserEmail(), request );
        return ResponseEntity.ok("토큰에서 코인으로 환전 요청이 완료되었습니다");
    }


    @Operation(
            summary = "토큰 정보를 조회 ",
            description = "토큰의 총 발행량 코인 갯수를 조회한다",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ActorAuth
    @GetMapping("/info")
    public ResponseEntity<TokenInfoResponse> getTokenInfo(@RequestParam String currency) {
        return ResponseEntity.ok(actorExchangeService.getBankTokenInfo(currency));
    }
}

