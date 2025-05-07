package dev.crepe.domain.auth;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN"),
    BANK("BANK"),
    SELLER("SELLER");

    private final String name;

    UserRole(String name) {this.name = name;}

    public boolean contains(String role) {
        return Arrays.stream(this.getName().split(","))
                .anyMatch(avail -> avail.equals(role));
    }


    public String getSpringSecurityRole() {
        return "ROLE_" + this.name();
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isUser() {
        return this == USER;
    }

    public boolean isSeller() {
        return this == SELLER;
    }

    public boolean isBank() { return this == BANK; }
}
