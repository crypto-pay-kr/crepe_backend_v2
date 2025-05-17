package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.admin.dto.response.GetAllBankTokenResponse;
import dev.crepe.domain.admin.service.AdminBankManageService;
import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.regulation.exception.TokenHistoryNotFoundException;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.TokenHistoryRepository;
import dev.crepe.domain.core.util.history.token.service.impl.PortfolioHistoryServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBankManageServiceImpl implements AdminBankManageService {


    private final BankService bankService;
    private final AccountService accountService;
    private final PortfolioHistoryServiceImpl portfolioHistoryService;
    private final BankTokenRepository bankTokenRepository;
    private final TokenHistoryRepository tokenHistoryRepository;


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

        // 모든 TokenHistory 조회
        return bankTokenRepository.findAll(pageRequest)
                .stream()
                .flatMap(bankToken -> bankToken.getTokenHistories().stream())
                .map(tokenHistory -> {
                    // PortfolioHistoryDetail 매핑
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
                            .collect(Collectors.toList());

                    // TokenHistoryResponse 생성
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
                .collect(Collectors.toList());
    }

    // 은행 토큰 발행 요청 승인
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveBankTokenRequest(Long tokenHistoryId) {

        // TokenHistory 조회
        TokenHistory tokenHistory = tokenHistoryRepository.findById(tokenHistoryId)
                .orElseThrow(() -> new TokenHistoryNotFoundException(tokenHistoryId));
        BankToken bankToken = tokenHistory.getBankToken();

        // 계좌 활성화
        accountService.activeBankTokenAccount(bankToken);
        // 포토폴리오 변경 내역 추가
        portfolioHistoryService.updatePortfolio(bankToken);
        // 토큰 발행 내역 추가
        portfolioHistoryService.updateTokenHistoryStatus( tokenHistoryId, BankTokenStatus.APPROVED);

        // 토큰 발행 승인
        bankToken.approve();
        bankTokenRepository.save(bankToken);

    }


    // 은행 토큰 발행 요청 반려
    @Transactional
    @Override
    public void rejectBankTokenRequest(RejectBankTokenRequest request, Long tokenHistoryId) {

        // TokenHistory 조회
        TokenHistory tokenHistory = tokenHistoryRepository.findById(tokenHistoryId)
                .orElseThrow(() -> new TokenHistoryNotFoundException(tokenHistoryId));

        // 토큰 발행 상태 변경
        portfolioHistoryService.updateTokenHistoryStatus(request, tokenHistory.getId(),  BankTokenStatus.REJECTED);

    }
}
