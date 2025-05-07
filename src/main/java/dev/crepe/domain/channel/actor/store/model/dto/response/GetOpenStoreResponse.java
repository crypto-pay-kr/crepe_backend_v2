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
public class GetOpenStoreResponse {
    private Long storeId;
    private String storeName;
    private String storeImage;
    private Long likeCount;
    private String storeType;
    private List<Coin> coinList;

}
