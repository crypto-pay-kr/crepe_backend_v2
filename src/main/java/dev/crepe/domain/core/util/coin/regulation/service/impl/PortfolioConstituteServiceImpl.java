package dev.crepe.domain.core.util.coin.regulation.service.impl;


import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioConstituteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioConstituteServiceImpl implements PortfolioConstituteService {

    private final AccountRepository accountRepository;


    // 포토폴리오 구성 유효성 검증
    @Override
    public void validatePortfolioConstitute(List<CreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail) {

        if (coinInfoList == null || coinInfoList.size() < 2) {
            throw new IllegalArgumentException("포트폴리오 구성은 최소 2개 이상의 코인으로 이루어져야 합니다.");
        }

        for (CreateBankTokenRequest.CoinInfo coin : coinInfoList) {
            if (coin.getAmount() == null || coin.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("포트폴리오의 각 코인의 수량(amount)은 0보다 커야 합니다.");
            }

            accountRepository.findByBank_EmailAndCoin_Currency(bankEmail, coin.getCurrency())
                    .orElseThrow(() -> new AccountNotFoundException("코인 " + coin.getCoinName() + "에 대한 계좌가 존재하지 않습니다."));
        }

    }
}
