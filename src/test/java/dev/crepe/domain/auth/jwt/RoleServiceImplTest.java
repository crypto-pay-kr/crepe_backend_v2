package dev.crepe.domain.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.util.*;
import dev.crepe.domain.auth.role.service.impl.RoleServiceImpl;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @InjectMocks
    private RoleServiceImpl roleService;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("사용자의 역할이 일치할 때 true 반환")
    void matchWithRole_WhenRoleMatches() {
        // given
        JwtAuthentication authentication = new JwtAuthentication("user@example.com", UserRole.USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // when
        boolean result = roleService.matchWithRole("USER");

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("사용자의 역할이 일치하지 않을 때 false 반환")
    void matchWithRole_WhenRoleDoesNotMatch() {
        // given
        JwtAuthentication authentication = new JwtAuthentication("user@example.com", UserRole.USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // when
        boolean result = roleService.matchWithRole("ADMIN");

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("어드민 역할이 어드민 요청과 일치할 때 true 반환")
    void matchWithRole_AdminRole() {
        // given
        JwtAuthentication authentication = new JwtAuthentication("admin@example.com", UserRole.ADMIN);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // when
        boolean result = roleService.matchWithRole("ADMIN");

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("판매자 역할이 판매자 요청과 일치할 때 true 반환")
    void matchWithRole_SellerRole() {
        // given
        JwtAuthentication authentication = new JwtAuthentication("seller@example.com", UserRole.SELLER);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // when
        boolean result = roleService.matchWithRole("SELLER");

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("인증 객체가 JwtAuthentication이 아닐 때 예외 발생")
    void matchWithRole_WhenAuthenticationIsNotJwtAuthentication() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        assertThrows(AccessDeniedException.class, () -> roleService.matchWithRole("USER"));
    }
}