package dev.crepe.domain.auth.sse.service.impl;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.model.entity.JwtToken;
import dev.crepe.domain.auth.jwt.repository.TokenRepository;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;
import dev.crepe.domain.auth.jwt.util.JwtTokenProvider;
import dev.crepe.domain.auth.sse.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final DuplicateLoginNotificationService notificationService;

    /**
     * 토큰 생성 및 저장 (중복 로그인 방지 + 실시간 알림)
     */
    @Override
    public AuthenticationToken createAndSaveToken(String email, UserRole role) {
        // 1. 기존 토큰 확인
        boolean hasExistingToken = tokenRepository.findByUserEmail(email).isPresent();

        // 2. JWT 토큰 생성
        AuthenticationToken token = jwtTokenProvider.createToken(email, role);

        // 3. 기존 토큰 무효화 후 새 토큰 저장
        saveOrUpdateToken(email, role, token);

        // 4. 기존 토큰이 있었다면 SSE로 실시간 알림 전송
        if (hasExistingToken) {
            notificationService.notifyDuplicateLogin(email);
            log.info("Sent duplicate login notification to: {}", email);
        }

        log.info("Token created and saved for user: {} with role: {}", email, role);
        return token;
    }

    /**
     * 토큰 저장 또는 업데이트 (기존 토큰 자동 무효화)
     */
    @Override
    public void saveOrUpdateToken(String userEmail, UserRole role, AuthenticationToken token) {
        Optional<JwtToken> existingToken = tokenRepository.findByUserEmail(userEmail);

        if (existingToken.isPresent()) {
            // 기존 토큰 업데이트 (기존 토큰은 자동으로 무효화됨)
            existingToken.get().updateTokens(token.getAccessToken(), token.getRefreshToken());
            log.info("Updated existing token for user: {}", userEmail);
        } else {
            // 새 토큰 저장
            JwtToken newToken = new JwtToken(userEmail, role, token.getAccessToken(), token.getRefreshToken());
            tokenRepository.save(newToken);
            log.info("Saved new token for user: {}", userEmail);
        }
    }

    /**
     * 개별 토큰 저장 (액세스, 리프레시 토큰을 따로 받을 때)
     */
    @Override
    public void saveOrUpdateToken(String userEmail, UserRole role, String accessToken, String refreshToken) {
        Optional<JwtToken> existingToken = tokenRepository.findByUserEmail(userEmail);

        if (existingToken.isPresent()) {
            // 기존 토큰 업데이트
            existingToken.get().updateTokens(accessToken, refreshToken);
            log.info("Updated existing token for user: {}", userEmail);
        } else {
            // 새 토큰 저장
            JwtToken newToken = new JwtToken(userEmail, role, accessToken, refreshToken);
            tokenRepository.save(newToken);
            log.info("Saved new token for user: {}", userEmail);
        }
    }

    /**
     * 로그아웃 - 토큰 무효화 및 SSE 연결 해제
     */
    @Override
    public void logout(String userEmail) {
        try {
            // 1. DB에서 토큰 삭제
            tokenRepository.deleteByUserEmail(userEmail);

            // 2. SSE 연결 해제
            notificationService.disconnectUser(userEmail);

            log.info("User logged out successfully - email: {}", userEmail);
        } catch (Exception e) {
            log.error("Error during logout for email: {}", userEmail, e);
            throw new RuntimeException("Logout failed", e);
        }
    }

    /**
     * 토큰 재발급
     */
    @Override
    public AuthenticationToken reissueToken(String refreshToken, String email, UserRole role) {
        // 1. 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // 2. DB의 리프레시 토큰과 비교
        JwtToken storedToken = tokenRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No stored token found"));

        if (!refreshToken.equals(storedToken.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh token mismatch");
        }

        // 3. 새 토큰 발급 및 저장
        AuthenticationToken newTokens = jwtTokenProvider.createToken(email, role);
        storedToken.updateTokens(newTokens.getAccessToken(), newTokens.getRefreshToken());

        log.info("Token reissued successfully for user: {}", email);
        return newTokens;
    }

    /**
     * 강제 로그아웃 (관리자 기능)
     */
    @Override
    public void forceLogout(String userEmail) {
        logout(userEmail);
        log.info("Force logout executed for user: {}", userEmail);
    }

    /**
     * 토큰 유효성 확인
     */
    @Override
    public boolean isValidToken(String email, String accessToken) {
        return tokenRepository.findByUserEmail(email)
                .map(jwtToken -> jwtToken.isMatchingAccessToken(accessToken))
                .orElse(false);
    }

    /**
     * 사용자의 저장된 토큰 조회
     */
    public Optional<JwtToken> getUserToken(String email) {
        return tokenRepository.findByUserEmail(email);
    }

    /**
     * 특정 역할의 모든 사용자 강제 로그아웃
     */
    @Override
    public void forceLogoutByRole(UserRole role) {
        try {
            tokenRepository.findAllByRole(role).forEach(token -> {
                // SSE 알림 전송
                notificationService.notifyDuplicateLogin(token.getUserEmail());

                // 토큰 삭제
                tokenRepository.deleteByUserEmail(token.getUserEmail());

                log.info("Force logout user: {} with role: {}", token.getUserEmail(), role);
            });
        } catch (Exception e) {
            log.error("Error during force logout by role: {}", role, e);
            throw new RuntimeException("Force logout by role failed", e);
        }
    }

    /**
     * 현재 활성 토큰 개수 조회 (모니터링용)
     */
    public long getActiveTokenCount() {
        return tokenRepository.count();
    }

    /**
     * 특정 사용자의 토큰 존재 여부 확인
     */
    public boolean hasActiveToken(String email) {
        return tokenRepository.existsByUserEmail(email);
    }

    /**
     * 모든 토큰 무효화 (긴급 상황용)
     */
    public void invalidateAllTokens() {
        try {
            // 모든 사용자에게 알림 전송
            tokenRepository.findAll().forEach(token -> {
                notificationService.notifyDuplicateLogin(token.getUserEmail());
            });

            // 모든 토큰 삭제
            tokenRepository.deleteAll();

            log.warn("All tokens have been invalidated");
        } catch (Exception e) {
            log.error("Error during invalidate all tokens", e);
            throw new RuntimeException("Failed to invalidate all tokens", e);
        }
    }


}