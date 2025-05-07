package dev.crepe.domain.auth.jwt;

public interface AuthenticationToken {
    String getAccessToken();

    String getRefreshToken();
}
