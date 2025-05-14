package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.core.util.coin.regulation.model.dto.request.GenerateTokenRequest;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import org.springframework.http.ResponseEntity;

public interface BankTokenGenerateService {

    ResponseEntity<Void> requestTokenGenerate(GenerateTokenRequest request, String bankEmail);

}
