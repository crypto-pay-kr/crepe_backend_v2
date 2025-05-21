package dev.crepe.domain.auth.jwt.util;


import dev.crepe.domain.auth.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

@AllArgsConstructor
public class JwtAuthentication implements AppAuthentication {

    private String userEmail;
    private UserRole userRole;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for(String authority : userRole.getName().split(",")) {
            authorities.add(() -> authority);
        }
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return userEmail;
    }

    @Override
    public Object getDetails() {
        return userEmail;
    }

    @Override
    public Object getPrincipal() {
        return userEmail;
    }

    @Override
    public boolean isAuthenticated() { return true;}

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {

    }

    @Override
    public String getUserEmail() {
        return userEmail;
    }

    @Override
    public UserRole getUserRole() {
        return userRole;
    }

    @Override
    public boolean isAdmin() {
        return userRole.isAdmin();
    }

    @Override
    public String getName() {
        return userEmail;
    }
}
