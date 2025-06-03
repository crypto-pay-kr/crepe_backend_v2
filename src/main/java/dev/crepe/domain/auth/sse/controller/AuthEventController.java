package dev.crepe.domain.auth.sse.controller;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.model.entity.JwtToken;
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
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
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

        // 토큰 만료 확인
        if (jwtTokenProvider.isTokenExpired(token)) {
            log.error("만료된 토큰으로 SSE 연결 시도: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new IllegalArgumentException("Token is expired");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            log.error("유효하지 않은 토큰: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new IllegalArgumentException("Invalid token");
        }

        JwtAuthentication auth = jwtTokenProvider.getAuthentication(token);
        String userEmail = auth.getUserEmail();

        log.info("SSE 연결 등록: {} (토큰 남은 시간: {}초)",
                userEmail, jwtTokenProvider.getTokenExpirationSeconds(token));

        return notificationService.registerUser(userEmail);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody TokenRequest tokenRequest) {
        try {
            String token = tokenRequest.getToken();
            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(createResponse(false, "Token is null", null));
            }

            boolean isValid = jwtTokenProvider.validateToken(token);
            boolean isExpired = jwtTokenProvider.isTokenExpired(token);
            boolean isExpiringSoon = jwtTokenProvider.isTokenExpiringSoon(token);
            long remainingSeconds = jwtTokenProvider.getTokenExpirationSeconds(token);

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("isValid", isValid);
            tokenInfo.put("isExpired", isExpired);
            tokenInfo.put("isExpiringSoon", isExpiringSoon);
            tokenInfo.put("remainingSeconds", remainingSeconds);

            if (isValid) {
                JwtAuthentication auth = jwtTokenProvider.getAuthentication(token);
                tokenInfo.put("userEmail", auth.getUserEmail());
                tokenInfo.put("userRole", auth.getUserRole().name());

                log.debug("토큰 검증 성공 - 사용자: {}, 남은 시간: {}초",
                        auth.getUserEmail(), remainingSeconds);

                return ResponseEntity.ok(createResponse(true, "Valid token", tokenInfo));
            } else {
                log.debug("토큰 검증 실패 - 만료됨: {}, 남은 시간: {}초", isExpired, remainingSeconds);
                return ResponseEntity.badRequest()
                        .body(createResponse(false, "Invalid or expired token", tokenInfo));
            }
        } catch (Exception e) {
            log.error("토큰 검증 오류", e);
            return ResponseEntity.badRequest()
                    .body(createResponse(false, "Token validation error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<Map<String, Object>> reissueToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            String userEmail = request.get("userEmail");
            String userRoleStr = request.get("userRole");

            log.info("토큰 재발행 요청 - 사용자: {}, 역할: {}", userEmail, userRoleStr);

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                log.warn("리프레시 토큰이 없음 - 사용자: {}", userEmail);
                return ResponseEntity.badRequest()
                        .body(createResponse(false, "Refresh token is required", null));
            }

            // 리프레시 토큰 만료 확인
            if (jwtTokenProvider.isTokenExpired(refreshToken)) {
                log.warn("리프레시 토큰 만료 - 사용자: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createResponse(false, "Refresh token is expired", null));
            }

            // 사용자 정보가 없으면 리프레시 토큰에서 추출 시도
            if (userEmail == null || userRoleStr == null) {
                try {
                    userEmail = jwtTokenProvider.getEmailFromToken(refreshToken);
                    userRoleStr = jwtTokenProvider.getRoleFromToken(refreshToken);
                    log.debug("리프레시 토큰에서 사용자 정보 추출 - 이메일: {}, 역할: {}", userEmail, userRoleStr);
                } catch (Exception e) {
                    log.warn("리프레시 토큰에서 사용자 정보 추출 실패", e);
                    return ResponseEntity.badRequest()
                            .body(createResponse(false, "Unable to extract user information from refresh token", null));
                }
            }

            if (userEmail == null || userRoleStr == null) {
                log.warn("사용자 정보 부족 - 이메일: {}, 역할: {}", userEmail, userRoleStr);
                return ResponseEntity.badRequest()
                        .body(createResponse(false, "User email and role are required", null));
            }

            UserRole userRole = UserRole.valueOf(userRoleStr);

            Optional<JwtToken> storedTokenOpt = authService.getUserToken(userEmail);
            if (storedTokenOpt.isEmpty()) {
                log.warn("저장된 토큰을 찾을 수 없음 - 사용자: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createResponse(false, "No stored token found", null));
            }

            JwtToken storedToken = storedTokenOpt.get();
            if (!refreshToken.equals(storedToken.getRefreshToken())) {
                log.warn("저장된 리프레시 토큰과 불일치 - 사용자: {}", userEmail);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createResponse(false, "Refresh token mismatch", null));
            }

            // ✅ 새로운 액세스 토큰 생성 (리프레시 토큰은 유지)
            String newAccessToken = jwtTokenProvider.createAccessToken(userEmail, userRole);

            // ✅ 저장된 토큰의 액세스 토큰만 업데이트
            storedToken.updateTokens(newAccessToken, refreshToken);

            // 재발급 성공 알림
            notificationService.notifyTokenRefreshed(userEmail);

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("accessToken", newAccessToken);
            tokenData.put("refreshToken", refreshToken); // 기존 리프레시 토큰 유지
            tokenData.put("userEmail", userEmail);
            tokenData.put("userRole", userRole.name());
            tokenData.put("expirationSeconds", jwtTokenProvider.getTokenExpirationSeconds(newAccessToken));

            log.info("토큰 재발급 성공 - 사용자: {}, 새 토큰 만료 시간: {}초, 리프레시 토큰 유지",
                    userEmail, jwtTokenProvider.getTokenExpirationSeconds(newAccessToken));

            return ResponseEntity.ok(createResponse(true, "Token reissued successfully", tokenData));

        } catch (IllegalArgumentException e) {
            log.warn("토큰 재발급 실패 - 잘못된 인수: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("토큰 재발급 중 예상치 못한 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponse(false, "Token reissue failed: " + e.getMessage(), null));
        }
    }

    /**
     * 토큰 만료 임박 확인 엔드포인트
     */
    @PostMapping("/check-expiry")
    public ResponseEntity<Map<String, Object>> checkTokenExpiry(@RequestBody TokenRequest tokenRequest) {
        try {
            String token = tokenRequest.getToken();
            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(createResponse(false, "Token is required", null));
            }

            boolean isExpired = jwtTokenProvider.isTokenExpired(token);
            boolean isExpiringSoon = jwtTokenProvider.isTokenExpiringSoon(token);
            long remainingSeconds = jwtTokenProvider.getTokenExpirationSeconds(token);

            Map<String, Object> expiryInfo = new HashMap<>();
            expiryInfo.put("isExpired", isExpired);
            expiryInfo.put("isExpiringSoon", isExpiringSoon);
            expiryInfo.put("remainingSeconds", remainingSeconds);
            expiryInfo.put("shouldReissue", isExpiringSoon && !isExpired);

            String message;
            if (isExpired) {
                message = "Token is expired";
            } else if (isExpiringSoon) {
                message = "Token will expire soon";
            } else {
                message = "Token is valid";
            }

            return ResponseEntity.ok(createResponse(true, message, expiryInfo));

        } catch (Exception e) {
            log.error("토큰 만료 확인 오류", e);
            return ResponseEntity.badRequest()
                    .body(createResponse(false, "Token expiry check error: " + e.getMessage(), null));
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