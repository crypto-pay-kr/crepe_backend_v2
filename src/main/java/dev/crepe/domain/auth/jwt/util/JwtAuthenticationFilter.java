package dev.crepe.domain.auth.jwt.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // upbit 관련 URL은 JWT 검증 제외
        if (uri.startsWith("/upbit")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = jwtTokenProvider.resolveToken(request);

            if (token != null && !token.trim().isEmpty()) {
                try {
                    // 토큰 유효성 검사
                    if (jwtTokenProvider.validateToken(token)) {
                        JwtAuthentication auth = jwtTokenProvider.getAuthentication(token);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Successfully set authentication for user: {}", auth.getUserEmail());
                    } else {
                        log.debug("Invalid token detected, clearing security context");
                        SecurityContextHolder.clearContext();
                    }
                } catch (io.jsonwebtoken.security.SignatureException e) {
                    log.warn("JWT signature validation failed for URI: {} - {}", uri, e.getMessage());
                    SecurityContextHolder.clearContext();
                } catch (io.jsonwebtoken.ExpiredJwtException e) {
                    log.debug("JWT token expired for URI: {} - {}", uri, e.getMessage());
                    SecurityContextHolder.clearContext();
                } catch (io.jsonwebtoken.MalformedJwtException e) {
                    log.warn("Malformed JWT token for URI: {} - {}", uri, e.getMessage());
                    SecurityContextHolder.clearContext();
                } catch (Exception e) {
                    log.error("Unexpected JWT processing error for URI: {} - {}", uri, e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            } else {
                // 토큰이 없는 경우 security context 초기화
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            log.error("Critical error in JWT authentication filter for URI: {}", uri, e);
            SecurityContextHolder.clearContext();
        }

        // 예외 발생 여부와 관계없이 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}