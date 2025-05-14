package dev.crepe.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {

    }

    public static String getRoleByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 null이거나 빈 값일 수 없습니다.");
        }

        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth== null || !auth.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        // Jwt에서 역할(Role) 정보 추출
        if (auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(role -> role.equals("BANK") || role.equals("ACTOR"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("유효한 역할이 없습니다."));
        }

        throw new IllegalStateException("권한 정보가 없습니다.");
    }
}
