package dev.crepe.domain.channel.actor.store.model.dto.response;

import dev.crepe.domain.channel.actor.store.model.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStoreStatusResponse {

    private StoreStatus storeStatus;
}
