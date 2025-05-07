package dev.crepe.domain.channel.actor.store.model.dto.request;

import dev.crepe.domain.channel.actor.store.model.CoinStatus;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "가맹점 지원 코인 수정 요청 DTO")
public class ChangeCoinStatusRequest {
    @Schema(description = "코인종류(SOL,USDT,XRP)", example = "SOL")
    private List<Coin> supportedCoins;
}