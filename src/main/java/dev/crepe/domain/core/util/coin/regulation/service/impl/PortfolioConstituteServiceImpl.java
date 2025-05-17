package dev.crepe.domain.core.util.coin.regulation.service.impl;


import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.coin.regulation.exception.InvalidPortfolioException;
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

    private static final int MIN_PORTFOLIO_SIZE = 2;
    private final AccountRepository accountRepository;

    // 포토폴리오 구성 유효성 검증
    @Override
    public void validatePortfolioConstitute(List<CreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail) {

        if (coinInfoList == null || coinInfoList.size() < MIN_PORTFOLIO_SIZE) {
            throw new InvalidPortfolioException("portfolio.invalid.size", MIN_PORTFOLIO_SIZE);
        }

        for (CreateBankTokenRequest.CoinInfo coin : coinInfoList) {
            if (coin.getAmount() == null || coin.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidPortfolioException("portfolio.invalid.amount", coin.getCoinName());
            }

            accountRepository.findByBank_EmailAndCoin_CurrencyAndAddressRegistryStatus(
                    bankEmail,
                    coin.getCurrency(),
                    AddressRegistryStatus.ACTIVE
            ).orElseThrow(() -> new AccountNotFoundException(bankEmail));


        }

    }


    // 포토폴리오 재구성 유효성 검증
    @Override
    public void revalidatePortfolioConstitute(List<ReCreateBankTokenRequest.CoinInfo> coinInfoList, String bankEmail) {

        if (coinInfoList == null || coinInfoList.size() < MIN_PORTFOLIO_SIZE) {
            throw new InvalidPortfolioException("portfolio.invalid.size", MIN_PORTFOLIO_SIZE);
        }

        for (ReCreateBankTokenRequest.CoinInfo coin : coinInfoList) {
            if (coin.getAmount() == null || coin.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidPortfolioException("portfolio.invalid.amount", coin.getCoinName());
            }

            // 새로운 계좌 추가 시 계좌가 있는지 확인
            accountRepository.findByBank_EmailAndCoin_CurrencyAndAddressRegistryStatus(
                    bankEmail,
                    coin.getCurrency(),
                    AddressRegistryStatus.ACTIVE
            ).orElseThrow(() -> new AccountNotFoundException(bankEmail));
        }
    }
}
