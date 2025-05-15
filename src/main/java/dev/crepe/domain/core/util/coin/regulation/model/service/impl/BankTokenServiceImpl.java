package dev.crepe.domain.core.util.coin.regulation.model.service.impl;


import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.repository.PortfolioRepository;
import dev.crepe.domain.core.util.coin.regulation.model.service.BankTokenService;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
@Service
@RequiredArgsConstructor
public class BankTokenServiceImpl implements BankTokenService {

    private final PortfolioRepository portfolioRepository;
    private final UpbitExchangeService upbitExchangeService;

    @Override
    public BigDecimal getTokenPrice(String tokenCurrency) {

        //1. 포트 폴리오조회
        List<Portfolio> portfolios = portfolioRepository.findAllByBankToken_Currency(tokenCurrency);
        if (portfolios.isEmpty()) {
            throw new IllegalArgumentException("해당 토큰의 포트폴리오가 존재하지 않습니다.");
        }

        //2. 총 자본금 조회
        BigDecimal totalKRW = BigDecimal.ZERO;
        for (Portfolio portfolio : portfolios) {
            BigDecimal coinAmount = portfolio.getAmount();
//            BigDecimal coinPrice = upbitExchangeService.getCoinPrice(portfolio.getCoin().getCurrency());
//            totalKRW = totalKRW.add(coinAmount.multiply(coinPrice));
        }

        //3. 뱅크 토큰 총 발행량
        BigDecimal totalSupply =portfolios.get(0).getBankToken().getTotalSupply();

        //4. 자본금 / 발행량 = 토큰 한개당 가격반환
        return totalKRW.divide(totalSupply, 8, RoundingMode.HALF_UP);
    }
}
