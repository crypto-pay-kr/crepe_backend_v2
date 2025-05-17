package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.bank.exception.BankNameMismatchException;
import dev.crepe.domain.bank.exception.BankNotFoundException;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;
import dev.crepe.domain.bank.model.dto.response.GetTokenHistoryResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankTokenService;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.regulation.exception.BankTokenNotFoundException;
import dev.crepe.domain.core.util.coin.regulation.exception.PendingBankTokenExistsException;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.service.TokenSetupService;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioConstituteService;
import dev.crepe.domain.core.util.history.token.repository.TokenHistoryRepository;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import dev.crepe.global.model.dto.GetPaginationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankTokenServiceImpl implements BankTokenService {

    private final PortfolioConstituteService portfolioConstituteService;
    private final TokenSetupService tokenSetupService;
    private final AccountService accountService;
    private final UpbitExchangeService upbitExchangeService;
    private final BankRepository bankRepository;
    private final AccountRepository accountRepository;
    private final BankTokenRepository bankTokenRepository;
    private final TokenHistoryRepository tokenHistoryRepository;

    // 은행 토큰 생성
    @Override
    public void createBankToken(CreateBankTokenRequest request, String bankEmail) {

        Bank bank = bankRepository.findByEmail(bankEmail)
                .orElseThrow(() -> new BankNotFoundException(bankEmail));

        if (!bank.getName().equals(request.getBankName())) {
            throw new BankNameMismatchException(request.getBankName(), bank.getName());
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
        BankToken bankToken = tokenSetupService.requestTokenGenerate(request, bankEmail);

        // PENDING 상태 계좌 생성
        accountService.createBankTokenAccount(bankToken);

    }

    // 은행 토큰 재발행
    @Override
    public void recreateBankToken(ReCreateBankTokenRequest request, String bankEmail) {

        Bank bank = bankRepository.findByEmail(bankEmail)
                .orElseThrow(() -> new BankNotFoundException(bankEmail));

        BankToken bankToken = bankTokenRepository.findByBank(bank)
                .orElseThrow(() -> new BankTokenNotFoundException(bank.getName()));

        if (tokenHistoryRepository.findByBankTokenAndStatus(bankToken, BankTokenStatus.PENDING).isPresent()) {
            throw new PendingBankTokenExistsException(bankToken.getName());
        }

        // 포토폴리오 재구성 정보 유효성 검증
        portfolioConstituteService.revalidatePortfolioConstitute(request.getPortfolioCoins(),  bankEmail);


        // 시세 오차 허용 범위 충족 여부 검증
        request.getPortfolioCoins().forEach(coin -> {
            upbitExchangeService.validateRateWithinThreshold(
                    coin.getCurrentPrice(),
                    coin.getCurrency(),
                    BigDecimal.valueOf(1)
            );
        });

        // bankToken 발행 요청 service 호출
        bankToken = tokenSetupService.requestTokenReGenerate(request, bankEmail);

        accountService.updateBankTokenAccount(bankToken);

    }


    // 생성된 토큰 계좌 조회
    @Override
    @Transactional(readOnly = true)
    public GetTokenAccountInfoResponse getAccountByBankToken(String bankEmail) {

        Bank bank = bankRepository.findByEmail(bankEmail)
                .orElseThrow(() -> new BankNotFoundException(bankEmail));

        BankToken bankToken = bankTokenRepository.findByBank(bank)
                .orElseThrow(() -> new BankTokenNotFoundException(bank.getName()));

        return accountRepository.findByBankAndBankTokenAndAddressRegistryStatus(
                bank,
                bankToken,
                AddressRegistryStatus.ACTIVE
        ).map(account -> GetTokenAccountInfoResponse.builder()
                .bankName(bank.getName())
                .tokenName(bankToken.getName())
                .tokenCurrency(bankToken.getCurrency())
                .balance(account.getBalance())
                .nonAvailableBalance(account.getNonAvailableBalance())
                .accountAddress(account.getAccountAddress())
                .build()
        ).orElseGet(() -> GetTokenAccountInfoResponse.builder().build());
    }


    // 토큰 정보 및 발행 내역 조회
    @Override
    @Transactional(readOnly = true)
    public List<GetTokenHistoryResponse> getTokenHistory(GetPaginationRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());

        Bank bank = bankRepository.findByEmail(request.getAuthEmail())
                .orElseThrow(() -> new BankNotFoundException(request.getAuthEmail()));

        // 특정 은행의 TokenHistory 조회
        return bankTokenRepository.findByBank_Id(bank.getId(), pageRequest)
                .stream()
                .flatMap(bankToken -> bankToken.getTokenHistories().stream())
                .map(tokenHistory -> {
                    // PortfolioHistoryDetail 매핑
                    List<GetTokenHistoryResponse.PortfolioDetail> portfolioDetails = tokenHistory.getPortfolioDetails()
                            .stream()
                            .map(detail -> GetTokenHistoryResponse.PortfolioDetail.builder()
                                    .coinName(detail.getCoinName())
                                    .coinCurrency(detail.getCoinCurrency())
                                    .prevAmount(detail.getPrevAmount())
                                    .prevPrice(detail.getPrevPrice())
                                    .updateAmount(detail.getUpdateAmount())
                                    .updatePrice(detail.getUpdatePrice())
                                    .build())
                            .collect(Collectors.toList());

                    // TokenHistoryResponse 생성
                    return GetTokenHistoryResponse.builder()
                            .tokenHistoryId(tokenHistory.getId())
                            .bankTokenId(tokenHistory.getBankToken().getId())
                            .totalSupplyAmount(tokenHistory.getTotalSupplyAmount())
                            .changeReason(tokenHistory.getChangeReason())
                            .rejectReason(tokenHistory.getRejectReason())
                            .requestType(tokenHistory.getRequestType())
                            .status(tokenHistory.getStatus())
                            .createdAt(tokenHistory.getCreatedAt())
                            .portfolioDetails(portfolioDetails)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
