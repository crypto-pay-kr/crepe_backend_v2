package dev.crepe.domain.auth.role;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@RoleService.matchWithRole('SELLER') or @RoleService.matchWithRole('USER')")
public @interface ActorAuth {
}