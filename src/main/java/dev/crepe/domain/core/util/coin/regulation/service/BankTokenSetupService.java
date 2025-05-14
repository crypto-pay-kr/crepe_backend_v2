package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import org.springframework.http.ResponseEntity;

public interface BankTokenSetupService {

    ResponseEntity<Void> requestTokenGenerate(CreateBankTokenRequest request, String bankEmail);

}
