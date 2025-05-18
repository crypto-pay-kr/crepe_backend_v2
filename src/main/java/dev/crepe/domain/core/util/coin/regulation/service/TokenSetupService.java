package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;

public interface TokenSetupService {

    BankToken requestTokenGenerate(CreateBankTokenRequest request, String bankEmail);

    BankToken requestTokenReGenerate(ReCreateBankTokenRequest request, String bankEmail);

}
