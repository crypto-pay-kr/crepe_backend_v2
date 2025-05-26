package dev.crepe.domain.channel.actor.user.model.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private String email;
    private String name;
    private String nickname;
    private String phoneNumber;
    private String role;
}