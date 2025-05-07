package dev.crepe.domain.auth.jwt;

import dev.crepe.domain.auth.UserRole;
import org.springframework.security.core.Authentication;

public interface AppAuthentication extends Authentication {
    String getUserEmail();

    UserRole getUserRole();

    boolean isAdmin();
}
