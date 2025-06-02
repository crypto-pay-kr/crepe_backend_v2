package dev.crepe.domain.bank.service.impl;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.response.GetTokenAccountInfoResponse;
import dev.crepe.domain.bank.model.dto.response.GetTokenHistoryResponse;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.bank.service.BankTokenManageService;
import dev.crepe.domain.core.account.model.AddressRegistryStatus;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenInfoService;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioService;
import dev.crepe.domain.core.util.coin.regulation.service.TokenPriceService;
import dev.crepe.domain.core.util.coin.regulation.service.TokenSetupService;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.service.TokenHistoryService;
import dev.crepe.domain.core.util.upbit.Service.UpbitExchangeService;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.model.dto.GetPaginationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankTokenManageServiceImpl implements BankTokenManageService {

    private final BankService bankService;
    private final AccountService accountService;
    private final UpbitExchangeService upbitExchangeService;
    private final BankTokenInfoService bankTokenInfoService;
    private final TokenSetupService tokenSetupService;
    private final TokenHistoryService tokenHistoryService;
    private final PortfolioService portfolioService;
    private final BankTokenRepository bankTokenRepository;
    private final TokenPriceService tokenPriceService;
    private final ExceptionDbService exceptionDbService;
    private final SubscribeHistoryRepository subscribeHistoryRepository;



    // 은행 토큰 생성
    @Override
    public void createBankToken(CreateBankTokenRequest request, String bankEmail) {
        log.info("은행 토큰 생성 시작 - 은행 이메일: {}", bankEmail);

        Bank bank = bankService.findBankInfoByEmail(bankEmail);

        // 포토폴리오 구성 정보 유효성 검증
        portfolioService.validatePortfolioConstitute(request.getPortfolioCoins(),  bankEmail);

        // 시세 오차 허용 범위 충족 여부 검증
        request.getPortfolioCoins().forEach(coin -> {
            upbitExchangeService.validateRateWithinThreshold(
                    coin.getCurrentPrice(),
                    coin.getCurrency(),
                    BigDecimal.valueOf(1)
            );
        });
        // bankToken 발행 요청 service 호출
        BankToken bankToken = tokenSetupService.requestTokenGenerate(request, bank);

        // PENDING 상태 계좌 생성
        accountService.createBankTokenAccount(bankToken);

        log.info("은행 토큰 생성 완료 - 토큰 이름: {}, 상태: {}", bankToken.getName(), bankToken.getStatus());

    }

    // 은행 토큰 재발행
    @Override
    public void recreateBankToken(ReCreateBankTokenRequest request, String bankEmail) {
        log.info("은행 토큰 재발행 시작 - 은행 이메일: {}", bankEmail);

        Bank bank = bankService.findBankInfoByEmail(bankEmail);

        BankToken bankToken = bankTokenInfoService.findByBank(bank);

        if (tokenHistoryService.findByBankTokenAndStatus(bankToken, BankTokenStatus.PENDING).isPresent()) {
            throw exceptionDbService.getException("BANK_TOKEN_002");
        }

        // 포토폴리오 재구성 정보 유효성 검증
        portfolioService.revalidatePortfolioConstitute(request.getPortfolioCoins(),  bankEmail);

        // 시세 오차 허용 범위 충족 여부 검증
        request.getPortfolioCoins().forEach(coin -> {
            upbitExchangeService.validateRateWithinThreshold(
                    coin.getCurrentPrice(),
                    coin.getCurrency(),
                    BigDecimal.valueOf(1)
            );
        });

        // bankToken 발행 요청 service 호출
        bankToken = tokenSetupService.requestTokenReGenerate(request, bank);

        accountService.updateBankTokenAccount(bankToken);
        log.info("은행 토큰 재발행 완료 - 토큰 이름: {}, 상태: {}", bankToken.getName(), bankToken.getStatus());
    }


    // 생성된 토큰 계좌 조회
    @Override
    @Transactional(readOnly = true)
    public GetTokenAccountInfoResponse getAccountByBankToken(String bankEmail) {
        log.info("토큰 계좌 조회 시작 - 은행 이메일: {}", bankEmail);
        Bank bank = bankService.findBankInfoByEmail(bankEmail);

        BankToken bankToken = bankTokenInfoService.findByBank(bank);

        GetTokenAccountInfoResponse response = accountService.findByBankAndBankTokenAndAddressRegistryStatus(
                bank,
                bankToken,
                AddressRegistryStatus.ACTIVE
        ).map(account -> GetTokenAccountInfoResponse.builder()
                .bankName(bank.getName())
                .tokenName(bankToken.getName())
                .tokenCurrency(bankToken.getCurrency())
                .balance(account.getBalance().toPlainString())
                .nonAvailableBalance(account.getNonAvailableBalance().toPlainString())
                .accountAddress(account.getAccountAddress())
                .build()
        ).orElseGet(() -> GetTokenAccountInfoResponse.builder().build());

        log.info("토큰 계좌 조회 완료 - 은행 이름: {}, 토큰 이름: {}", bank.getName(), bankToken.getName());
        return response;
    }


    // 토큰 정보 및 발행 내역 조회
    @Override
    @Transactional(readOnly = true)
    public List<GetTokenHistoryResponse> getTokenHistory(GetPaginationRequest request) {
        log.info("토큰 발행 내역 조회 시작 - 요청 이메일: {}", request.getAuthEmail());
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());

        Bank bank = bankService.findBankInfoByEmail(request.getAuthEmail());

        List<TokenHistory> tokenHistories = tokenHistoryService.findTokenHistoriesByBank(bank, pageRequest);

        List<GetTokenHistoryResponse> response = tokenHistories.stream()
                .map(tokenHistory -> {
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

                    return GetTokenHistoryResponse.builder()
                            .bankName(bank.getName())
                            .tokenHistoryId(tokenHistory.getId())
                            .bankTokenId(tokenHistory.getBankToken().getId())
                            .tokenName(tokenHistory.getBankToken().getName())
                            .currency(tokenHistory.getBankToken().getCurrency())
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

        log.info("토큰 발행 내역 조회 완료 - 총 내역 수: {}", response.size());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BankToken getBankTokenByEmail(String bankEmail) {
        log.info("은행 토큰 조회 시작 - 은행 이메일: {}", bankEmail);

        return bankTokenRepository.findByBankEmail(bankEmail)
                .orElseThrow(() -> exceptionDbService.getException("BANK_TOKEN_001"));

    }
  
    // 은행 토큰 시세 조회
    public BigDecimal getLatestTokenPrice(String bankEmail) {
        Bank bank = bankService.findBankInfoByEmail(bankEmail);
        BankToken bankToken = bankTokenInfoService.findByBank(bank);
        return tokenPriceService.getPreviousTotalPrice(bankToken);
    }

    // 은행 토큰 거래량 조회
    public BigDecimal getTotalTokenVolume(String bankEmail) {
        return subscribeHistoryRepository.sumAmountByBankEmail(bankEmail);
    }

}


