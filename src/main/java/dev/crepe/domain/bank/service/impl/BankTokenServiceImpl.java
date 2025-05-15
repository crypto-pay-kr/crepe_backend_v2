package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankTokenService;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenSetupService;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioConstituteService;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BankTokenServiceImpl implements BankTokenService {

    private final PortfolioConstituteService portfolioConstituteService;
    private final BankTokenSetupService bankTokenSetupService;
    private final AccountService accountService;
    private final UpbitExchangeService upbitExchangeService;
    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;
    private final BankTokenRepository bankTokenRepository;

    // 은행 토큰 생성
    @Override
    public void createBankToken(CreateBankTokenRequest request, String bankEmail) {

        // email로 bankName을 받아 request.bankName과 일치하는지 검증
        Bank bank = bankRepository.findByEmail(bankEmail)
                .orElseThrow(() -> new IllegalArgumentException("은행을 찾을 수 없습니다."));

        if(!bank.getName().equals(request.getBankName())) {
            throw new IllegalArgumentException("은행 이름이 일치하지 않습니다.");
        }

        // 포토폴리오 구성 정보 유효성 검증
        portfolioConstituteService.validatePortfolioConstitute(request.getPortfolioCoins(),  bankEmail);

        // 시세 오차 허용 범위 충족 여부 검증
        request.getPortfolioCoins().forEach(coin -> {
            upbitExchangeService.validateRateWithinThreshold(
                    coin.getCurrentPrice(),
                    coin.getCurrency(),
                    BigDecimal.valueOf(1)
            );
        });
        // bankToken 발행 요청 service 호출
        BankToken bankToken = bankTokenSetupService.requestTokenGenerate(request, bankEmail);

        // PENDING 상태 계좌 생성
        accountService.createBankTokenAccount(bankToken);

    }

    // 은행 토큰 재발행
    @Override
    public void reCreateBankToken(ReCreateBankTokenRequest request, String bankEmail) {

        // email로 bankName을 받아 request.bankName과 일치하는지 검증
        Bank bank = bankRepository.findByEmail(bankEmail)
                .orElseThrow(() -> new IllegalArgumentException("은행을 찾을 수 없습니다."));

        if(!bank.getName().equals(request.getBankName())) {
            throw new IllegalArgumentException("은행 이름이 일치하지 않습니다.");
        }

        // 포토폴리오 재구성 정보 유효성 검증
        portfolioConstituteService.reValidatePortfolioConstitute(request.getPortfolioCoins(),  bankEmail);


        // 시세 오차 허용 범위 충족 여부 검증
        request.getPortfolioCoins().forEach(coin -> {
            upbitExchangeService.validateRateWithinThreshold(
                    coin.getCurrentPrice(),
                    coin.getCurrency(),
                    BigDecimal.valueOf(1)
            );
        });

        // bankToken 발행 요청 service 호출
        BankToken bankToken = bankTokenSetupService.requestTokenReGenerate(request, bankEmail);

        // 계좌 PENDING 상태로 변경
        accountService.updateBankTokenAccount(bankToken);

    }


    // 생성된 토큰 계좌 조회
    @Override
    @Transactional(readOnly = true)
    public GetTokenAccountInfoResponse getAccountByBankToken(String bankEmail) {
        Bank bank = bankRepository.findByEmail(bankEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 은행을 찾을 수 없습니다."));
        BankToken bankToken = bankTokenRepository.findByBank(bank)
                .orElseThrow(() -> new IllegalArgumentException("은행이 보유한 토큰이 없습니다."));
        return accountRepository.findByBankAndBankTokenAndAddressRegistryStatus(
                bank,
                bankToken,
                AddressRegistryStatus.ACTIVE
        ).map(account -> GetTokenAccountInfoResponse.builder()
                .bankName(bank.getName())
                .tokenName(bankToken.getName())
                .tokenCurrency(bankToken.getCurrency())
                .balance(account.getBalance())
                .accountAddress(account.getAccountAddress())
                .build()
        ).orElseGet(() -> GetTokenAccountInfoResponse.builder().build());
    }
}
