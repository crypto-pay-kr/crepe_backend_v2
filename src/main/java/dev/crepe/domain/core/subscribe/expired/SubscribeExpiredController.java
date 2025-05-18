package dev.crepe.domain.core.subscribe.expired;

import dev.crepe.domain.auth.jwt.AppAuthentication;
import dev.crepe.domain.auth.role.UserAuth;
import dev.crepe.domain.core.subscribe.expired.service.SubscribeExpiredService;
import dev.crepe.domain.core.subscribe.expired.service.SubscribeTerminateService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/expired")
@RequiredArgsConstructor
public class SubscribeExpiredController {

    private final SubscribeExpiredService subscribeExpiredService;
    private final SubscribeTerminateService subscribeTerminateService;

    @PostMapping("/{subscribeId}")
    @UserAuth
    @Operation(summary = "상품 만기 해지", description = "해당 상품이 만기 해지되었습니다.")
    public ResponseEntity<?> expired(@PathVariable Long subscribeId, AppAuthentication auth) {
        log.info("🔥 [EXPIRED 요청] subscribeId={}, user={}", subscribeId, auth.getUserEmail());
        String result = subscribeExpiredService.expired(auth.getUserEmail(),subscribeId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/terminate/{subscribeId}")
    @UserAuth
    @Operation(summary = "상품 중도 해지", description = "해당 상품이 중도 해지되었습니다.")
    public ResponseEntity<?> terminate(@PathVariable Long subscribeId, AppAuthentication auth) {
        log.info("🛑 [TERMINATE 요청] subscribeId={}, user={}", subscribeId, auth.getUserEmail());
        String result = subscribeTerminateService.terminate(auth.getUserEmail(),subscribeId);
        return ResponseEntity.ok(result);
    }


}

