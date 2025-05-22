package dev.crepe.domain.core.subscribe.expired;


import dev.crepe.domain.auth.jwt.util.AppAuthentication;
import dev.crepe.domain.auth.role.ActorAuth;
import dev.crepe.domain.core.subscribe.expired.service.SubscribeExpiredService;
import dev.crepe.domain.core.subscribe.expired.service.SubscribeTerminateService;
import dev.crepe.domain.core.subscribe.model.dto.response.TerminatePreviewDto;
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
    @ActorAuth
    @Operation(summary = "상품 만기 해지", description = "해당 상품이 만기 해지되었습니다.")
    public ResponseEntity<?> expired(@PathVariable Long subscribeId, AppAuthentication auth) {
        String result = subscribeExpiredService.expired(auth.getUserEmail(),subscribeId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/terminate/{subscribeId}")
    @ActorAuth
    @Operation(summary = "상품 중도 해지", description = "해당 상품이 중도 해지되었습니다.")
    public ResponseEntity<?> terminate(@PathVariable Long subscribeId, AppAuthentication auth) {
        String result = subscribeTerminateService.terminate(auth.getUserEmail(),subscribeId);
        return ResponseEntity.ok(result);
    }


}

