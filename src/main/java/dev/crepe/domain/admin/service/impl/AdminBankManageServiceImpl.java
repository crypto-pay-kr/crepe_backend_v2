package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.dto.response.GetAllBankTokenResponse;
import dev.crepe.domain.admin.service.AdminBankManageService;
import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.history.token.model.entity.TokenPortfolioHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBankManageServiceImpl implements AdminBankManageService {


    private final BankService bankService;
    private final BankTokenRepository bankTokenRepository;
    private final AccountRepository accountRepository;


    // 은행 계정 생성
    @Override
    @Transactional
    public void bankSignup(BankSignupDataRequest request, MultipartFile bankCiImage) {

        BankDataRequest bankDataRequest = new BankDataRequest(request, bankCiImage);
        bankService.signup(bankDataRequest);
    }

    // 토큰 생성 요청  목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<GetAllBankTokenResponse> getAllBankTokenResponseList(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        return bankTokenRepository.findAll(pageRequest)
                .stream()
                .map(bankToken -> {
                    // TokenHistory에서 가장 최근 기록 가져오기
                    TokenPortfolioHistory latestHistory = bankToken.getTokenHistories()
                            .stream()
                            .max(Comparator.comparing(TokenPortfolioHistory::getCreatedAt))
                            .orElse(null);

                    // Portfolio 데이터를 매핑하여 CoinInfo 리스트 생성
                    List<GetAllBankTokenResponse.CoinInfo> portfolioCoins = bankToken.getPortfolios()
                            .stream()
                            .map(portfolio -> GetAllBankTokenResponse.CoinInfo.builder()
                                    .coinName(portfolio.getCoin().getName())
                                    .amount(portfolio.getAmount())
                                    .currency(portfolio.getCoin().getCurrency())
                                    .currentPrice(portfolio.getInitialPrice())
                                    .build())
                            .collect(Collectors.toList());

                    // GetPendingBankTokenResponse 생성
                    return GetAllBankTokenResponse.builder()
                            .bankTokenId(bankToken.getId())
                            .bankName(bankToken.getBank().getName())
                            .createdAt(bankToken.getCreatedAt())
                            .tokenName(bankToken.getName())
                            .tokenCurrency(bankToken.getCurrency())
                            .portfolioCoins(portfolioCoins)
                            .status(bankToken.getStatus())
                            .totalSupply(latestHistory != null ? latestHistory.getAmount() : null)
                            .description(latestHistory != null ? latestHistory.getDescription() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 은행 토큰 발행 요청 승인
    @Override
    @Transactional
    public void approveBankTokenRequest(Long tokenId) {
        // BankToken 조회
        BankToken bankToken = bankTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("해당 토큰 발행 요청을 찾을 수 없습니다."));

        // 가장 최근의 TokenHistory 가져오기
        TokenPortfolioHistory latestHistory = bankToken.getTokenHistories()
                .stream()
                .max(Comparator.comparing(TokenPortfolioHistory::getCreatedAt))
                .orElseThrow(() -> new IllegalArgumentException("토큰 히스토리가 존재하지 않습니다."));

        // 계좌 상태 업데이트
        Account account = accountRepository.findByBankAndBankToken(bankToken.getBank(), bankToken)
                .orElseThrow(() -> new IllegalArgumentException("해당 토큰에 대한 계좌를 찾을 수 없습니다."));
        account.approveAddress();

        bankToken.updateStatus(BankTokenStatus.APPROVED);
    }


    // 은행 토큰 발행 요청 반려
    @Transactional
    @Override
    public void rejectBankTokenRequest(Long tokenId) {

        // BankToken 조회
        BankToken bankToken = bankTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("해당 토큰 발행 요청을 찾을 수 없습니다."));

        // 가장 최근의 TokenHistory 가져오기
        TokenPortfolioHistory latestHistory = bankToken.getTokenHistories()
                .stream()
                .max(Comparator.comparing(TokenPortfolioHistory::getCreatedAt))
                .orElseThrow(() -> new IllegalArgumentException("토큰 히스토리가 존재하지 않습니다."));

        // 계좌 상태 업데이트
        Account account = accountRepository.findByBankAndBankToken(bankToken.getBank(), bankToken)
                .orElseThrow(() -> new IllegalArgumentException("해당 토큰에 대한 계좌를 찾을 수 없습니다."));
        account.rejectAddress();

        bankToken.updateStatus(BankTokenStatus.REJECTED);
    }
}
