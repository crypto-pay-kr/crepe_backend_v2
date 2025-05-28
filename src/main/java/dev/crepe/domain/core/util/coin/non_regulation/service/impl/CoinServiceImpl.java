package dev.crepe.domain.core.util.coin.non_regulation.service.impl;

import dev.crepe.domain.core.util.coin.model.GetCoinInfo;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.coin.non_regulation.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {

    private final CoinRepository coinRepo;

    public Coin findByCurrency(String currency) {
        return coinRepo.findByCurrency(currency);
    }


    @Override
    public GetCoinInfo coinInfoService(String currency) {

        Coin coin = coinRepo.findByCurrency(currency);

        return GetCoinInfo.builder()
                .currency(coin.getCurrency())
                .address(coin.getAddress())
                .tag(coin.getTag())
                .build();
    }





}