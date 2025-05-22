package dev.crepe.domain.channel.actor.model.dto.response;

import dev.crepe.domain.core.subscribe.model.dto.response.SubscribeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class BankTokenAccountDto {
    private Long bankTokenId;
    private Long bankId;
    private String bankTokenName;
    private List<String> balances;
    private String currency;
    private List<SubscribeResponseDto> products;
}
