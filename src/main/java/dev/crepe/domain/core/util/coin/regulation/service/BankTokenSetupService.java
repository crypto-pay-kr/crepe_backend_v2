package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.http.ResponseEntity;

public interface BankTokenSetupService {

    BankToken requestTokenGenerate(CreateBankTokenRequest request, String bankEmail);

}
