package dev.crepe.domain.core.util.coin.non_regulation.service;

import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoinService {

    private final CoinRepository coinRepository;

    public Coin findByCurrency(String currency) {
        return coinRepository.findByCurrency(currency);
    }
}
