package dev.crepe.domain.channel.actor.store.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeBusinessInfoResponse {
    private String businessNumber;
    private String businessImg;
}
