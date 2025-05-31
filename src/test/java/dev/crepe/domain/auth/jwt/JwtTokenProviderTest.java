package dev.crepe.domain.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;
import dev.crepe.domain.auth.jwt.util.JwtAuthentication;
import dev.crepe.domain.auth.jwt.util.JwtTokenProvider;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForUnitTestingPurposesOnlyDoNotUseInProduction",
    "jwt.access-expiration=3600s",
    "jwt.refresh-expiration=86400s"
})
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private String validToken;
    private final String testEmail = "test@example.com";
    private final UserRole testRole = UserRole.USER;

    @BeforeEach
    void setUp() {
        AuthenticationToken token = jwtTokenProvider.createToken(testEmail, testRole);
        validToken = token.getAccessToken();
    }

    @Test
    @DisplayName("토큰 생성 테스트")
    void createToken() {
        // when
        AuthenticationToken token = jwtTokenProvider.createToken(testEmail, testRole);
        
        // then
        assertNotNull(token);
        assertNotNull(token.getAccessToken());
        assertNotNull(token.getRefreshToken());
    }

    @Test
    @DisplayName("리프레시 토큰 생성 테스트")
    void createRefreshToken() {
        // when
        String refreshToken = jwtTokenProvider.createRefreshToken(testEmail, testRole);
        
        // then
        assertNotNull(refreshToken);
    }

    @Test
    @DisplayName("토큰으로부터 인증 정보 가져오기 테스트")
    void getAuthentication() {
        // when
        JwtAuthentication authentication = jwtTokenProvider.getAuthentication(validToken);
        
        // then
        assertNotNull(authentication);
        assertEquals(testEmail, authentication.getUserEmail());
        assertEquals(testRole, authentication.getUserRole());
    }

    @Test
    @DisplayName("HTTP 요청에서 토큰 추출 테스트")
    void resolveToken() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        
        // when
        String resolvedToken = jwtTokenProvider.resolveToken(request);
        
        // then
        assertEquals(validToken, resolvedToken);
    }

    @Test
    @DisplayName("HTTP 요청에서 Bearer 접두사 없는 경우 null 반환 테스트")
    void resolveTokenWithoutBearerPrefix() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", validToken);
        
        // when
        String resolvedToken = jwtTokenProvider.resolveToken(request);
        
        // then
        assertNull(resolvedToken);
    }

    @Test
    @DisplayName("유효한 토큰 검증 테스트")
    void validateToken() {
        // when
        boolean isValid = jwtTokenProvider.validateToken(validToken);
        
        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 테스트")
    void validateInvalidToken() {
        // given
        String invalidToken = "invalid.token.string";
        
        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("토큰 재발급 테스트")
    void reissue() {
        // given
        String refreshToken = jwtTokenProvider.createRefreshToken(testEmail, testRole);
        
        // when
        AuthenticationToken reissuedToken = jwtTokenProvider.reissue(refreshToken, testEmail, testRole);
        
        // then
        assertNotNull(reissuedToken);
        assertNotNull(reissuedToken.getAccessToken());
        assertNotNull(reissuedToken.getRefreshToken());
    }
}