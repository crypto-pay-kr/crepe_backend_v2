package dev.crepe.global.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import dev.crepe.domain.auth.jwt.util.JwtAuthentication;
import dev.crepe.domain.auth.jwt.util.JwtTokenProvider;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCLoggingFilter implements Filter {
    
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_EMAIL_KEY = "userEmail";
    
    private JwtTokenProvider jwtTokenProvider;
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        
        // favicon.ico나 기타 정적 리소스는 스킵
        if (uri.equals("/favicon.ico") || uri.startsWith("/static/") || uri.startsWith("/css/") || uri.startsWith("/js/")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        
        try {
            if (jwtTokenProvider == null) {
                WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(
                    request.getServletContext());
                if (context != null) {
                    jwtTokenProvider = context.getBean(JwtTokenProvider.class);
                }
            }

            // Request ID 생성 및 설정
            String requestId = request.getHeader(REQUEST_ID_HEADER);
            if (!StringUtils.hasText(requestId)) {
                requestId = UUID.randomUUID().toString().substring(0, 8);
            }
            
            // MDC에 값 설정
            MDC.put(REQUEST_ID_KEY, requestId);
            String userEmail = getCurrentUserEmail(request);
            if (userEmail == null) {
                userEmail = "anonymous";
            }
            MDC.put(USER_EMAIL_KEY, userEmail);
            MDC.put("method", method);
            MDC.put("uri", uri);
            MDC.put("remoteAddr", getClientIpAddress(request));
            
            // 디버깅 정보
            log.info("=== 요청 시작 === {} {}", method, uri);
            
            long startTime = System.currentTimeMillis();
            
            // 다음 필터로 진행
            filterChain.doFilter(servletRequest, servletResponse);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("=== 요청 완료 === {} {} ({}ms)", method, uri, duration);
            
        } finally {
            // MDC 정리
            MDC.clear();
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 여러 IP가 콤마로 구분될 수 있음
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * JWT 토큰에서 현재 사용자의 이메일을 추출
     * 
     * 방법 1: HTTP 요청에서 직접 JWT 토큰을 파싱하여 이메일 추출 (권장)
     * 방법 2: SecurityContext에서 인증 정보 가져오기 (JwtAuthenticationFilter 이후에만 가능)
     */
    private String getCurrentUserEmail(HttpServletRequest request) {
        try {
            // JwtTokenProvider가 사용 가능한 경우에만 토큰 파싱 시도
            if (jwtTokenProvider != null) {
                String token = jwtTokenProvider.resolveToken(request);
                if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                    JwtAuthentication authentication = jwtTokenProvider.getAuthentication(token);
                    return authentication.getUserEmail();
                }
            }
            
            // 방법 2: SecurityContext에서 가져오기 (backup, JwtAuthenticationFilter 이후에만 동작)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthentication) {
                JwtAuthentication jwtAuth = (JwtAuthentication) authentication;
                return jwtAuth.getUserEmail();
            }
            
        } catch (Exception e) {
            // 토큰 파싱 실패 시 로그 출력 (선택사항)
            log.error("Failed to extract user email from token: {}", e.getMessage(), e);
        }
        
        return null; // 인증되지 않은 사용자 또는 토큰이 없는 경우
    }
}