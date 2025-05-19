package dev.crepe.domain.core.util.coin.regulation.service.impl;

import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.TokenPrice;
import dev.crepe.domain.core.util.coin.regulation.repository.TokenPriceRepository;
import dev.crepe.domain.core.util.coin.regulation.service.TokenPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TokenPriceServiceImpl implements TokenPriceService {

    private final TokenPriceRepository tokenPriceRepository;

    @Override
    public void createAndSaveTokenPrice(BankToken bankToken, BigDecimal price) {
        TokenPrice tokenPrice = TokenPrice.builder()
                .bankToken(bankToken)
                .price(price)
                .build();
        tokenPriceRepository.save(tokenPrice);
    }
}