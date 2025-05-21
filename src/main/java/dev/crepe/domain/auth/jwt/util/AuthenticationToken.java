package dev.crepe.domain.auth.jwt.util;

public interface AuthenticationToken {
    String getAccessToken();

    String getRefreshToken();
}
