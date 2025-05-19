package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.core.util.coin.regulation.model.dto.request.TokenInfoResponse;

public interface BankTokenInfoService {



    TokenInfoResponse getTokenInfo(String tokenCurrency);
}
