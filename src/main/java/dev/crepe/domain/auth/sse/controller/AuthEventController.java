package dev.crepe.domain.auth.sse.controller;

import dev.crepe.domain.auth.jwt.util.JwtAuthentication;
import dev.crepe.domain.auth.jwt.util.JwtTokenProvider;
import dev.crepe.domain.auth.sse.model.TokenRequest;
import dev.crepe.domain.auth.sse.service.impl.DuplicateLoginNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*") // CORS 설정 추가
public class AuthEventController {

    private final DuplicateLoginNotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping(value = "/auth/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter authEvents(
            HttpServletRequest request,
            @RequestParam(value = "token", required = false) String tokenParam) {

        log.info("SSE 연결 요청 받음");

        String token = tokenParam != null ? tokenParam : jwtTokenProvider.resolveToken(request);

        if (token == null) {
            log.error("토큰이 없습니다.");
            throw new IllegalArgumentException("Token is required");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            log.error("유효하지 않은 토큰: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new IllegalArgumentException("Invalid token");
        }

        JwtAuthentication auth = jwtTokenProvider.getAuthentication(token);
        String userEmail = auth.getUserEmail();

        log.info("SSE 연결 등록: {}", userEmail);
        return notificationService.registerUser(userEmail);
    }

    @PostMapping("/auth/validate-token")
    public ResponseEntity<String> validateToken(@RequestBody TokenRequest tokenRequest) {
        try {
            String token = tokenRequest.getToken();
            if (token == null) {
                return ResponseEntity.badRequest().body("Token is null");
            }

            boolean isValid = jwtTokenProvider.validateToken(token);
            if (isValid) {
                JwtAuthentication auth = jwtTokenProvider.getAuthentication(token);
                return ResponseEntity.ok("Valid token for user: " + auth.getUserEmail());
            } else {
                return ResponseEntity.badRequest().body("Invalid token");
            }
        } catch (Exception e) {
            log.error("토큰 검증 오류", e);
            return ResponseEntity.badRequest().body("Token validation error: " + e.getMessage());
        }
    }

}
