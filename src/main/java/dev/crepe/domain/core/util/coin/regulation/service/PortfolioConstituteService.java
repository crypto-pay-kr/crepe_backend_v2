package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;

import java.util.List;

public interface PortfolioConstituteService {

    void validatePortfolioConstitute(List<CreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail);
}
