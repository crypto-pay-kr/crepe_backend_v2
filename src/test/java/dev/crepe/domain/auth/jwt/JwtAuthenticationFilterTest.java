package dev.crepe.domain.auth.jwt;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.crepe.domain.auth.UserRole;
import jakarta.servlet.ServletException;
import dev.crepe.domain.auth.jwt.util.*;

@SpringBootTest
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    private JwtAuthentication mockAuthentication;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        mockAuthentication = new JwtAuthentication("test@example.com", UserRole.USER);
        
        // 각 테스트 시작 전 SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증 토큰이 있을 때 필터 작동 테스트")
    void doFilterInternal_WithValidToken() throws ServletException, IOException {
        // given
        String token = "valid.token";
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtTokenProvider.resolveToken(request)).thenReturn(token);
        when(jwtTokenProvider.getAuthentication(token)).thenReturn(mockAuthentication);
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(mockAuthentication, SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProvider).resolveToken(request);
        verify(jwtTokenProvider).getAuthentication(token);
    }

    @Test
    @DisplayName("인증 토큰이 없을 때 필터 작동 테스트")
    void doFilterInternal_WithoutToken() throws ServletException, IOException {
        // given
        when(jwtTokenProvider.resolveToken(request)).thenReturn(null);
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtTokenProvider).resolveToken(request);
        verify(jwtTokenProvider, never()).getAuthentication(anyString());
    }
}