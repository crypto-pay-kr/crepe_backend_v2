package dev.crepe.domain.channel.actor.model.dto.response;


import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBankTokenInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class GetAllBalanceResponse {
    private List<GetBalanceResponse> balance;
    private List<GetBankTokenInfoResponse> bankTokenInfo;
}
