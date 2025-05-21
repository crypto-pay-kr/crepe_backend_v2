package dev.crepe.domain.auth.jwt.model.entity;

import dev.crepe.domain.auth.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "jwt_tokens")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JwtToken {

    @Id
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail; // 이메일을 Primary Key로 변경

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "access_token", nullable = false, length = 1000)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, length = 1000)
    private String refreshToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 이메일 기반 생성자
    public JwtToken(String userEmail, UserRole role, String accessToken, String refreshToken) {
        this.userEmail = userEmail;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 특정 액세스 토큰과 일치하는지 확인
     */
    public boolean isMatchingAccessToken(String token) {
        return this.accessToken != null && this.accessToken.equals(token);
    }

    /**
     * 특정 리프레시 토큰과 일치하는지 확인
     */
    public boolean isMatchingRefreshToken(String token) {
        return this.refreshToken != null && this.refreshToken.equals(token);
    }
}