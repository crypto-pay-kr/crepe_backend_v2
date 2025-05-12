package dev.crepe.domain.core.util.coin.non_regulation.service;

import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CoinInitService {

    private final CoinRepository coinRepository;

    @PostConstruct
    public void init() {
        saveIfNotExists("리플", "XRP", "XRP", true, "0.4", "https://example.com/xrp.png");
        saveIfNotExists("솔라나", "SOL", "SOL", false, "0.009", "https://example.com/sol.png");
        saveIfNotExists("테더", "USDT", "TRC20", false, "0", "https://example.com/usdt.png");
    }

    private void saveIfNotExists(String name, String currency, String networkType,
                                 boolean isTag, String minDeposit, String coinImage) {
        if (!coinRepository.existsByCurrency(currency)) {
            Coin coin = Coin.builder()
                    .name(name)
                    .currency(currency)
                    .networkType(networkType)
                    .isTag(isTag)
                    .minDeposit(minDeposit)
                    .coinImage(coinImage)
                    .createdAt(LocalDateTime.now())
                    .build();
            coinRepository.save(coin);
        }
    }
}