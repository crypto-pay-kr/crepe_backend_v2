package dev.crepe.domain.auth.sse.controller;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;
import dev.crepe.domain.auth.jwt.util.JwtAuthentication;
import dev.crepe.domain.auth.jwt.util.JwtTokenProvider;
import dev.crepe.domain.auth.sse.model.TokenRequest;
import dev.crepe.domain.auth.sse.service.impl.AuthServiceImpl;
import dev.crepe.domain.auth.sse.service.impl.DuplicateLoginNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*") // CORS 설정 추가
public class AuthEventController {

    private final DuplicateLoginNotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthServiceImpl authService;

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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

    @PostMapping("/validate-token")
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

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            String userEmail = request.get("userEmail");
            String userRoleStr = request.get("userRole");

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createResponse(false, "Refresh token is required", null));
            }

            // 사용자 정보가 없으면 리프레시 토큰에서 추출 시도
            if (userEmail == null || userRoleStr == null) {
                try {
                    JwtAuthentication auth = jwtTokenProvider.getAuthentication(refreshToken);
                    userEmail = auth.getUserEmail();
                    userRoleStr = auth.getRole().name();
                } catch (Exception e) {
                    log.warn("❌ 리프레시 토큰에서 사용자 정보 추출 실패", e);
                    return ResponseEntity.badRequest()
                            .body(createResponse(false, "Unable to extract user information", null));
                }
            }

            UserRole userRole = UserRole.valueOf(userRoleStr);

            // 토큰 재발급
            AuthenticationToken newTokens = authService.reissueToken(refreshToken, userEmail, userRole);

            // 재발급 성공 알림
            notificationService.notifyTokenRefreshed(userEmail);

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", newTokens.getAccessToken());
            tokenData.put("refreshToken", newTokens.getRefreshToken());
            tokenData.put("userEmail", userEmail);
            tokenData.put("userRole", userRole.name());

            log.info("🔄 토큰 재발급 성공 - 사용자: {}", userEmail);
            return ResponseEntity.ok(createResponse(true, "Token reissued successfully", tokenData));

        } catch (IllegalArgumentException e) {
            log.warn("❌ 토큰 재발급 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ 토큰 재발급 중 예상치 못한 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, "Token reissue failed", null));
        }
    }

    /**
     * 응답 객체 생성 헬퍼 메서드
     */
    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());

        if (data != null) {
            response.put("data", data);
        }

        return response;
    }

}
