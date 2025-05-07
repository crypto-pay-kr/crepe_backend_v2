package dev.crepe.domain.channel.actor.store.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GetStoreBalanceResponse {

    private String coinName;
    private String currency;
    private BigDecimal balance;

    public GetStoreBalanceResponse(String coinName, String currency, BigDecimal balance) {
        this.coinName = coinName;
        this.currency = currency;
        this.balance = balance;
    }
}
