package dev.crepe.global.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCLoggingFilter implements Filter {
    
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        
        try {
            // Request ID 설정
            String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
            if (!StringUtils.hasText(requestId)) {
                requestId = UUID.randomUUID().toString().replaceAll("-", "");
            }
            MDC.put(REQUEST_ID_KEY, requestId);
            
            // 추가적인 MDC 정보 설정 (옵션)
            MDC.put("method", httpRequest.getMethod());
            MDC.put("uri", httpRequest.getRequestURI());
            MDC.put("remoteAddr", getClientIpAddress(httpRequest));
            
            // 사용자 정보가 있다면 추가 (인증 후)
            // String userId = getCurrentUserId(httpRequest);
            // if (StringUtils.hasText(userId)) {
            //     MDC.put(USER_ID_KEY, userId);
            // }
            
            filterChain.doFilter(servletRequest, servletResponse);
            
        } finally {
            // 요청 처리 완료 후 MDC 정리
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
    
    // 인증된 사용자 정보를 가져오는 메서드 (예시)
    // private String getCurrentUserId(HttpServletRequest request) {
    //     // JWT 토큰에서 사용자 ID 추출하거나
    //     // Security Context에서 가져오기
    //     return null;
    // }
}