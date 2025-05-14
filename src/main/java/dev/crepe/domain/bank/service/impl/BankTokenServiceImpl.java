package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankTokenService;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenSetupService;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioConstituteService;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankTokenServiceImpl implements BankTokenService {

    private final PortfolioConstituteService portfolioConstituteService;
    private final BankTokenSetupService bankTokenSetupService;
    private final UpbitExchangeService upbitExchangeService;
    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;

    @Override
    public void createBankToken(CreateBankTokenRequest request, String bankEmail) {

        // email로 bankName을 받아 request.bankName과 일치하는지 검증
        Bank bank = bankRepository.findByEmail(bankEmail)
                .orElseThrow(() -> new IllegalArgumentException("은행을 찾을 수 없습니다."));

        if(!bank.getName().equals(request.getBankName())) {
            throw new IllegalArgumentException("은행 이름이 일치하지 않습니다.");
        }

        portfolioConstituteService.validatePortfolioConstitute(request.getPortfolioCoins(),  bankEmail);

        // 현재 시세 조회 -> upbitExchangeService 호출해서 코인별 시세 받아오고 오차 검증
        // 오차 허용 범위 내 확인 되면 다음 단계 진행
        request.getPortfolioCoins().forEach(coin -> {
            BigDecimal latestRate = upbitExchangeService.getLatestRate(coin.getCurrency());
            BigDecimal difference = coin.getCurrentPrice().subtract(latestRate).abs();
            System.out.println("코인: " + coin.getCoinName() + ", 현재 시세: " + latestRate + ", 요청 시세: " + coin.getCurrentPrice() + ", 오차: " + difference);
            if (difference.compareTo(BigDecimal.valueOf(25)) > 0) {
                throw new IllegalArgumentException("코인 시세 오차 허용 범위를 초과했습니다. (허용 범위: ±25)");
            }
        });
        // bankToken 발행 요청 service 호출
        bankTokenSetupService.requestTokenGenerate(request, bankEmail);


    }

    private void validateAccountExistence(List<CreateBankTokenRequest.CoinInfo> portfolioCoins, String bankEmail) {
        portfolioCoins.forEach(coin -> {
            accountRepository.findByBank_EmailAndCoin_Currency(bankEmail, coin.getCoinName())
                    .orElseThrow(() -> new AccountNotFoundException("코인 " + coin.getCoinName() + "에 대한 계좌가 존재하지 않습니다."));
        });

    }

}
