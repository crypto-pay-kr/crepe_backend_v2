package dev.crepe.domain.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    // PUBLIC_URI 경로 목록 추가
    private static final String[] PUBLIC_URI = {
            "/user/signup",
            "/store/signup",
            "/login",
            "/sms",
            "/ocr",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/api-docs",
            "/api",
            "/swagger-resources",
            "/captcha"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        // PUBLIC_URI 또는 /upbit로 시작하는 경로에 대해 인증 스킵
        if (uri.startsWith("/upbit") || shouldSkipAuthentication(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtTokenProvider.resolveToken(request);
        if (token != null) {
            JwtAuthentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 주어진 URI가 인증을 스킵해야 하는 PUBLIC_URI에 포함되는지 확인합니다.
     * @param uri 검사할 URI
     * @return PUBLIC_URI에 포함되면 true, 그렇지 않으면 false
     */
    private boolean shouldSkipAuthentication(String uri) {
        return Arrays.stream(PUBLIC_URI).anyMatch(uri::startsWith);
    }
}