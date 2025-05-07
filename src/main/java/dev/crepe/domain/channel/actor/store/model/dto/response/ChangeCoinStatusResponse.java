package dev.crepe.domain.channel.actor.store.model.dto.response;

import dev.crepe.domain.channel.actor.store.model.CoinStatus;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCoinStatusResponse {
    @ArraySchema(
            arraySchema = @Schema(description = "지원하는 코인 목록"),
            schema = @Schema(implementation = Coin.class, example = "BITCOIN")
    )
    private List<Coin> supportedCoins;
}
