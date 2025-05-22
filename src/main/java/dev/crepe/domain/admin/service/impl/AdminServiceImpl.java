package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.service.AdminService;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;
import dev.crepe.domain.auth.jwt.util.JwtTokenProvider;
import dev.crepe.domain.auth.jwt.model.entity.JwtToken;
import dev.crepe.domain.auth.jwt.repository.TokenRepository;
import dev.crepe.domain.auth.sse.service.AuthService;
import dev.crepe.domain.auth.sse.service.impl.AuthServiceImpl;
import dev.crepe.domain.channel.actor.exception.LoginFailedException;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.global.model.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ActorRepository actorRepository;
    private final PasswordEncoder encoder;
    private final AuthServiceImpl authService;

    // 관리자 로그인
    @Override
    @Transactional
    public ApiResponse<TokenResponse> adminLogin(LoginRequest request) {

        Actor actor = actorRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginFailedException::new);

        if (!encoder.matches(request.getPassword(), actor.getPassword())) {
            throw new LoginFailedException();
        }

        // 관리자 권한 확인 (선택사항)
        if (!actor.getRole().isAdmin()) {
            throw new LoginFailedException();
        }

        // AuthService를 통해 토큰 생성 및 저장 (중복 로그인 방지 + 실시간 알림)
        AuthenticationToken token = authService.createAndSaveToken(actor.getEmail(), actor.getRole());

        TokenResponse tokenResponse = new TokenResponse(token, actor);
        return ApiResponse.success(actor.getRole() + " 로그인 성공", tokenResponse);
    }

    // 추가: 관리자 기능 - 특정 사용자 강제 로그아웃
    public ApiResponse<?> forceLogoutUser(String userEmail) {
        try {
            authService.forceLogout(userEmail);
            return ApiResponse.success("사용자가 강제 로그아웃되었습니다.", null);
        } catch (Exception e) {
            log.error("Force logout failed for user: {}", userEmail, e);
            return ApiResponse.fail("강제 로그아웃 실패: " + e.getMessage());
        }
    }

    // 추가: 관리자 기능 - 특정 역할의 모든 사용자 강제 로그아웃
    public ApiResponse<?> forceLogoutByRole(String roleName) {
        try {
            UserRole role = UserRole.valueOf(roleName.toUpperCase());
            authService.forceLogoutByRole(role);
            return ApiResponse.success(role + " 역할의 모든 사용자가 강제 로그아웃되었습니다.", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail("잘못된 역할입니다: " + roleName);
        } catch (Exception e) {
            log.error("Force logout by role failed for role: {}", roleName, e);
            return ApiResponse.fail("역할별 강제 로그아웃 실패: " + e.getMessage());
        }
    }

    // 추가: 관리자 기능 - 활성 토큰 수 조회
    public ApiResponse<?> getActiveTokenCount() {
        try {
            long count = authService.getActiveTokenCount();
            return ApiResponse.success("활성 토큰 수 조회 성공", count);
        } catch (Exception e) {
            log.error("Get active token count failed", e);
            return ApiResponse.fail("활성 토큰 수 조회 실패: " + e.getMessage());
        }
    }

    // 추가: 관리자 기능 - 모든 토큰 무효화 (긴급 상황용)
    public ApiResponse<?> invalidateAllTokens() {
        try {
            authService.invalidateAllTokens();
            return ApiResponse.success("모든 토큰이 무효화되었습니다.", null);
        } catch (Exception e) {
            log.error("Invalidate all tokens failed", e);
            return ApiResponse.fail("모든 토큰 무효화 실패: " + e.getMessage());
        }
    }


}
