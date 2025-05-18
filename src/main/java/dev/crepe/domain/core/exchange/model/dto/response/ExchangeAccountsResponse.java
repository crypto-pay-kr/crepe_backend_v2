package dev.crepe.domain.core.exchange.model.dto.response;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class ExchangeAccountsResponse {

    private Account actorCoinAccount;
    private Account actorTokenAccount;
    private Account bankTokenAccount;
    private Account bankCoinAccount;
    private List<Portfolio> portfolios;
    private List<Account> bankCoinAccounts;
}
