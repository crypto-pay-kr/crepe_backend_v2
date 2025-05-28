package dev.crepe.domain.core.util.coin.non_regulation.service;

import dev.crepe.domain.core.util.coin.model.GetCoinInfo;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

public interface CoinService {

    Coin findByCurrency(String currency);
    GetCoinInfo coinInfoService(String currency);
}
