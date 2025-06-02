package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.dto.request.ChangeBankStatusRequest;
import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.admin.dto.response.*;
import dev.crepe.domain.admin.service.AdminBankManageService;
import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import dev.crepe.domain.bank.model.dto.response.GetBankDashboardResponse;
import dev.crepe.domain.bank.model.dto.response.GetCoinAccountInfoResponse;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.product.service.impl.ProductServiceImpl;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenInfoService;
import dev.crepe.domain.core.util.coin.regulation.service.TokenService;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBankManageServiceImpl implements AdminBankManageService {
    private final TokenService tokenService;
    private final BankService bankService;
    private final BankTokenInfoService bankTokenInfoService;
    private final ProductServiceImpl productService;
    private final AccountService accountService;
    private final ProductRepository productRepository;
    private final BankRepository bankRepository;
    private final BankTokenRepository bankTokenRepository;
    private final ExceptionDbService exceptionDbService;

    // 은행 계정 생성
    @Override
    @Transactional
    public void bankSignup(BankSignupDataRequest request, MultipartFile bankCiImage) {

        BankDataRequest bankDataRequest = new BankDataRequest(request, bankCiImage);
        bankService.signup(bankDataRequest);
    }

    // 전체 은행토큰 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<GetAllBankTokenResponse> getAllBankTokenResponseList(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        // BankToken 조회
        List<BankToken> bankTokens = bankTokenInfoService.findAllBankTokens(pageRequest);

        // 응답 생성
        return bankTokens.stream()
                .flatMap(bankToken -> bankToken.getTokenHistories().stream())
                .sorted((h1, h2) -> h2.getCreatedAt().compareTo(h1.getCreatedAt()))
                .map(tokenHistory -> {
                    List<GetAllBankTokenResponse.PortfolioDetail> portfolioDetails = tokenHistory.getPortfolioDetails()
                            .stream()
                            .map(detail -> GetAllBankTokenResponse.PortfolioDetail.builder()
                                    .coinName(detail.getCoinName())
                                    .coinCurrency(detail.getCoinCurrency())
                                    .prevAmount(detail.getPrevAmount())
                                    .prevPrice(detail.getPrevPrice())
                                    .updateAmount(detail.getUpdateAmount())
                                    .updatePrice(detail.getUpdatePrice())
                                    .build())
                            .toList();

                    return GetAllBankTokenResponse.builder()
                            .bankId(tokenHistory.getBankToken().getBank().getId())
                            .bankName(tokenHistory.getBankToken().getBank().getName())
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
                .toList();
    }

    // 은행 토큰 발행 요청 승인
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveBankTokenRequest(Long tokenHistoryId) {
        tokenService.approveBankTokenRequest(tokenHistoryId);
    }


    // 은행 토큰 발행 요청 반려
    @Transactional
    @Override
    public void rejectBankTokenRequest(RejectBankTokenRequest request, Long tokenHistoryId) {
        tokenService.rejectBankTokenRequest(request, tokenHistoryId);
    }

    @Override
    public List<GetAllProductResponse> getAllBankProducts(Long bankId) {
        return productService.getAllBankProducts(bankId);
    }

    @Override
    public List<GetAllProductResponse> getSuspendedBankProducts(Long bankId) {
        return productService.getSuspendedBankProducts(bankId);
    }

    @Override
    public List<GetAllBankResponse> getAllActiveBankInfoList() {
        return bankService.getAllActiveBankList();
    }

    @Override
    public void changeBankStatus(ChangeBankStatusRequest request) {
        bankService.changeBankStatus(request);
    }

    @Override
    public List<GetAllSuspendedBankResponse> getAllSuspendedBankInfoList() {
        return bankService.getAllSuspendedBankList();
    }

    @Override
    public List<GetCoinAccountInfoResponse> getBankAccountByAdmin(Long bankId) {
        return bankService.getBankAccountByAdmin(bankId);
    }

    @Override
    public void holdBankAddress(Long accountId) {

        Account account = accountService.getAccountById(accountId);

        // 소유주가 Bank인지 검증
        if (account.getBank() == null) {
            throw exceptionDbService.getException("ACCOUNT_011");
        }

        accountService.holdAccount(account);
    }

    public GetProductDetailResponse getBankProductDetail(Long bankId, Long productId) {
        return productService.getProductDetail(bankId,productId);
    }

    public GetBankDashboardResponse getBankDashboard() {
        long bankCount = bankRepository.count();
        long productCount = productRepository.count();
        long bankTokenCount = bankTokenRepository.count();

        return new GetBankDashboardResponse(bankCount, productCount, bankTokenCount);
    }


}
