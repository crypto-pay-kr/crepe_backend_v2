package dev.crepe.domain.channel.actor.store.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetMenuDetailResponse {
    private Long menuId;
    private String menuName;
    private int menuPrice;
    private String menuImage;
}

