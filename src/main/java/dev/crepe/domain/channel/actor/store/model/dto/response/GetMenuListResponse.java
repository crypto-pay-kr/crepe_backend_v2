package dev.crepe.domain.channel.actor.store.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetMenuListResponse {
    private List<GetMenuDetailResponse> menus;

}
