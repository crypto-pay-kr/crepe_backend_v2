package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.core.util.coin.regulation.model.dto.request.TokenInfoResponse;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface BankTokenInfoService {

    TokenInfoResponse getTokenInfo(String tokenCurrency);

    List<BankToken> findAllBankTokens(PageRequest pageRequest);

    void validateTokenNotAlreadyRequested(Long bankId);

    BankToken findByBank(Bank bank);

    void saveBankToken(BankToken bankToken);

}
