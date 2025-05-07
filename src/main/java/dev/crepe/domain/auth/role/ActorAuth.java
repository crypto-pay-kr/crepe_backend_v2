package dev.crepe.domain.auth.role;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("isAuthenticated()") // 인증된 사용자만 접근 가능
public @interface ActorAuth {
}