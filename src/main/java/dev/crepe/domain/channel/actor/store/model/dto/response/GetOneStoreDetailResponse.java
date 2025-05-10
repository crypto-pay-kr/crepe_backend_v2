package dev.crepe.domain.channel.actor.store.model.dto.response;

import dev.crepe.domain.channel.actor.store.model.CoinStatus;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetOneStoreDetailResponse {
    private Long likeCount;
    private String storeName;
    private String storeAddress;
    private String storeImageUrl;
    private List<Coin> coinList;
    private boolean isLiked;
    private List<GetMenuDetailResponse> menuList;
}
