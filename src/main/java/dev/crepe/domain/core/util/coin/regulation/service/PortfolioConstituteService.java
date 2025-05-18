package dev.crepe.domain.core.util.coin.regulation.service;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;

import java.util.List;

public interface PortfolioConstituteService {

    void validatePortfolioConstitute(List<CreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail);

    void revalidatePortfolioConstitute(List<ReCreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail);
}
