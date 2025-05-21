package dev.crepe.domain.auth.sse.service;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;

public interface AuthService {

    /**
     * 토큰 생성 및 저장 (중복 로그인 방지)
     */
    AuthenticationToken createAndSaveToken(String email, UserRole role);

    /**
     * 토큰 저장 또는 업데이트 (기존 토큰 자동 무효화)
     */
    void saveOrUpdateToken(String userEmail, UserRole role, AuthenticationToken token);

    /**
     * 개별 토큰 저장 (액세스, 리프레시 토큰을 따로 받을 때)
     */
    void saveOrUpdateToken(String userEmail, UserRole role, String accessToken, String refreshToken);

    /**
     * 로그아웃 - 토큰 무효화
     */
    void logout(String userEmail);

    /**
     * 토큰 재발급
     */
    AuthenticationToken reissueToken(String refreshToken, String email, UserRole role);

    /**
     * 강제 로그아웃 (관리자 기능)
     */
    void forceLogout(String userEmail);

    /**
     * 토큰 유효성 확인
     */
    boolean isValidToken(String email, String accessToken);

    /**
     * 특정 역할의 모든 사용자 강제 로그아웃
     */
    void forceLogoutByRole(UserRole role);
}