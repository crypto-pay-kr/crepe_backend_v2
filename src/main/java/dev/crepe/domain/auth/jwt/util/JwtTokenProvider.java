package dev.crepe.domain.auth.jwt.util;

import dev.crepe.domain.auth.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class JwtTokenProvider {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String secretKeyString;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    @Value("${jwt.access-expiration}")
    private Duration accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private Duration refreshExpiration;

    private String createToken(Map<String, Object> claims) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validity = now.plus(accessExpiration);

        log.debug("토큰 생성 - 현재시간: {}, 만료시간: {}, 유효기간: {}초",
                now, validity, accessExpiration.getSeconds());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject((String) claims.get("email"))
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(validity.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(getSigningKey())
                .compact();
    }

    public AuthenticationToken createToken(String email, UserRole role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role.name());

        String accessToken = createToken(claims);
        String refreshToken = createRefreshToken();

        log.info("새 토큰 생성 완료 - 사용자: {}, 역할: {}, Access 만료: {}초, Refresh 만료: {}초",
                email, role, accessExpiration.getSeconds(), refreshExpiration.getSeconds());

        return JwtAuthenticationToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String createRefreshToken() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validity = now.plus(refreshExpiration);

        return Jwts.builder()
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(validity.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(getSigningKey())
                .compact();
    }

    public JwtAuthentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);

        UserRole userRole = UserRole.valueOf(role);
        return new JwtAuthentication(email, userRole);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            boolean isValid = !claims.getBody().getExpiration().before(new Date());

            if (!isValid) {
                log.debug("토큰 만료됨 - 만료시간: {}, 현재시간: {}",
                        claims.getBody().getExpiration(), new Date());
            }

            return isValid;
        } catch (ExpiredJwtException e) {
            log.debug("토큰 만료 예외 발생 - 만료시간: {}", e.getClaims().getExpiration());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            boolean expired = claims.getExpiration().before(new Date());

            if (expired) {
                log.debug("토큰 만료 확인 - 만료시간: {}, 현재시간: {}",
                        claims.getExpiration(), new Date());
            }

            return expired;
        } catch (ExpiredJwtException e) {
            log.debug("토큰 만료 예외로 인한 만료 확인: {}", e.getClaims().getExpiration());
            return true;
        } catch (Exception e) {
            log.error("토큰 만료 확인 중 오류: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 토큰의 남은 유효 시간을 초 단위로 반환
     */
    public long getTokenExpirationSeconds(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            long remainingSeconds = (expiration.getTime() - now.getTime()) / 1000;
            log.debug("토큰 남은 시간: {}초", remainingSeconds);

            return Math.max(0, remainingSeconds);
        } catch (Exception e) {
            log.error("토큰 만료 시간 계산 오류: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 토큰이 곧 만료되는지 확인 (기본: 30초 이내)
     */
    public boolean isTokenExpiringSoon(String token) {
        return isTokenExpiringSoon(token, 30);
    }

    /**
     * 토큰이 지정된 시간(초) 이내에 만료되는지 확인
     */
    public boolean isTokenExpiringSoon(String token, long thresholdSeconds) {
        long remainingSeconds = getTokenExpirationSeconds(token);
        boolean expiringSoon = remainingSeconds > 0 && remainingSeconds <= thresholdSeconds;

        if (expiringSoon) {
            log.debug("토큰 만료 임박 - 남은 시간: {}초, 임계값: {}초", remainingSeconds, thresholdSeconds);
        }

        return expiringSoon;
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰에서 Claims 추출: {}", e.getClaims().getSubject());
            return e.getClaims();
        }
    }

    public AuthenticationToken reissue(String refreshToken, String email, UserRole role) {
        if (!validateToken(refreshToken)) {
            log.error("유효하지 않은 리프레시 토큰으로 재발행 시도 - 사용자: {}", email);
            throw new JwtException("Invalid refresh token");
        }

        log.info("토큰 재발행 성공 - 사용자: {}, 역할: {}", email, role);
        return createToken(email, role);
    }

    /**
     * 토큰에서 사용자 이메일 추출 (만료된 토큰에서도 가능)
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("토큰에서 이메일 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 토큰에서 사용자 역할 추출 (만료된 토큰에서도 가능)
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("토큰에서 역할 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    public String createAccessToken(String email, UserRole role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role.name()); // ✅ role.name() 사용

        String accessToken = createToken(claims);

        log.info("액세스 토큰만 재발급 - 사용자: {}, 역할: {}, 만료: {}초",
                email, role, accessExpiration.getSeconds());

        return accessToken;
    }
}