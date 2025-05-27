package dev.crepe.domain.auth.sse.model;

import lombok.Getter;

@Getter
public class TokenRequest {
    private String token;

    public void changeToken(String token){
        this.token = token;
    }

}
