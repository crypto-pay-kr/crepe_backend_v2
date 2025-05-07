package dev.crepe.domain.auth.role;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@RoleService.matchWithRole('USER')")
public @interface UserAuth {
}
