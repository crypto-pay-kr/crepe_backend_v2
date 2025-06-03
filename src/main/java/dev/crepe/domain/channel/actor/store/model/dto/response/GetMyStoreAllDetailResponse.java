package dev.crepe.domain.channel.actor.store.model.dto.response;

import dev.crepe.domain.channel.actor.store.model.StoreStatus;
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
public class GetMyStoreAllDetailResponse {
    private Long storeId;
    private String email;
    private Long likeCount;
    private StoreStatus storeStatus;
    private String storeName;
    private String storeAddress;
    private String storeImageUrl;
    private String storeNickname;
    private boolean isLiked;
    private List<Coin> coinList;
    private List<GetMenuDetailResponse> menuList;
}
