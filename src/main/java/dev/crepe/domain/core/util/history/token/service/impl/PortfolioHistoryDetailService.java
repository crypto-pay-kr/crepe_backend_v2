package dev.crepe.domain.core.util.history.token.service.impl;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.util.history.token.model.entity.PortfolioHistoryDetail;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.PortfolioHistoryDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PortfolioHistoryDetailService {


    private final PortfolioHistoryDetailRepository portfolioHistoryDetailRepository;

    public void savePortfolioHistoryDetails(TokenHistory tokenHistory, CreateBankTokenRequest.CoinInfo coinInfo) {
        PortfolioHistoryDetail detail = PortfolioHistoryDetail.builder()
                .tokenHistory(tokenHistory)
                .coinName(coinInfo.getCoinName())
                .coinCurrency(coinInfo.getCurrency())
                .updateAmount(coinInfo.getAmount())
                .updatePrice(coinInfo.getCurrentPrice())
                .build();
        portfolioHistoryDetailRepository.save(detail);
    }

    public void updatePortfolioHistoryDetails(TokenHistory tokenHistory, ReCreateBankTokenRequest.CoinInfo coinInfo, BigDecimal prevAmount, BigDecimal prevPrice) {
        PortfolioHistoryDetail detail = PortfolioHistoryDetail.builder()
                .tokenHistory(tokenHistory)
                .coinName(coinInfo.getCoinName())
                .coinCurrency(coinInfo.getCurrency())
                .prevAmount(prevAmount) // 이전 값 저장
                .prevPrice(prevPrice)   // 이전 값 저장
                .updateAmount(coinInfo.getAmount()) // 수정된 값
                .updatePrice(coinInfo.getCurrentPrice()) // 수정된 값
                .build();
        portfolioHistoryDetailRepository.save(detail);
    }
}
