package dev.crepe.domain.channel.actor.model.dto.response;


import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.AuthenticationToken;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import lombok.Getter;

import dev.crepe.global.base.UserBaseEntity;


@Getter
public class TokenResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String email;
    private final UserRole role;

    public TokenResponse(AuthenticationToken token, Actor actor) {
        this.accessToken = token.getAccessToken();
        this.refreshToken = token.getRefreshToken();
        this.email = actor.getEmail();
        this.role = actor.getRole();
    }

    public TokenResponse(AuthenticationToken token, Bank bank) {
        this.accessToken = token.getAccessToken();
        this.refreshToken = token.getRefreshToken();
        this.email = bank.getEmail();
        this.role = bank.getRole();
    }
}
