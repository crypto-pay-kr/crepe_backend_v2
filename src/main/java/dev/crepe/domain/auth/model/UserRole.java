package dev.crepe.domain.auth.model;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN"),
    BANK("BANK"),
    SELLER("SELLER");

    private final String name;

    UserRole(String name) {this.name = name;}


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
}
