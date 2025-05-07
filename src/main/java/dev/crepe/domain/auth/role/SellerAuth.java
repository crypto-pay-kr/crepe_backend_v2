package dev.crepe.domain.auth.role;


import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@RoleService.matchWithRole('SELLER')")
public @interface SellerAuth {
}
