package dev.crepe.domain.core.util.history.token.service.impl;

import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioService;
import dev.crepe.domain.core.util.history.token.model.TokenRequestType;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.TokenHistoryRepository;
import dev.crepe.domain.core.util.history.token.service.PortfolioHistoryService;
import dev.crepe.domain.core.util.history.token.service.TokenHistoryService;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioHistoryServiceImpl implements PortfolioHistoryService {


    private final TokenHistoryService tokenHistoryService;
    private final PortfolioService portfolioService;
    private final PortfolioHistoryDetailService portfolioHistoryDetailService;
    private final TokenHistoryRepository tokenHistoryRepository;
    private final ExceptionDbService exceptionDbService;


    // 토큰 포토폴리오 신규 내역 저장
    @Override
    @Transactional
    public void addTokenPortfolioHistory(CreateBankTokenRequest request, BankToken bankToken, BigDecimal totalSupplyAmount) {
        try {
            // TokenHistory 생성
            TokenHistory tokenHistory = tokenHistoryService.createTokenHistory(
                    bankToken, totalSupplyAmount, BankTokenStatus.PENDING, TokenRequestType.NEW);

            // PortfolioHistoryDetail 저장
            request.getPortfolioCoins().forEach(portfolio ->
                    portfolioHistoryDetailService.savePortfolioHistoryDetails(tokenHistory, portfolio)
            );
        } catch (Exception e) {
            throw exceptionDbService.getException("PORTFOLIO_001");
        }
    }

    // 토큰 재발행 요청 이력 업데이트
    @Override
    @Transactional
    public void updateTokenHistory(ReCreateBankTokenRequest request, BankToken bankToken, BigDecimal totalSupplyAmount) {
        try {
            // TokenHistory 생성
            TokenHistory tokenHistory = tokenHistoryService.createTokenHistory(
                    bankToken, totalSupplyAmount, BankTokenStatus.PENDING, TokenRequestType.UPDATE);

            // PortfolioHistoryDetail 추가
            request.getPortfolioCoins().forEach(portfolio -> {
                // 기존 Portfolio 데이터 조회
                var existingPortfolio = portfolioService.findByBankTokenAndCoinCurrency(bankToken, portfolio.getCurrency());

                // prevAmount와 prevPrice 설정
                BigDecimal prevAmount = existingPortfolio.map(p -> p.getAmount()).orElse(BigDecimal.ZERO);
                BigDecimal prevPrice = existingPortfolio.map(p -> p.getInitialPrice()).orElse(BigDecimal.ZERO);

                // PortfolioHistoryDetail 저장
                portfolioHistoryDetailService.updatePortfolioHistoryDetails(tokenHistory, portfolio, prevAmount, prevPrice);

            });
        } catch (Exception e) {
            throw exceptionDbService.getException("PORTFOLIO_002");
        }
    }


    // 토큰 발행 승인 이후 포토폴리오 업데이트
    @Override
    @Transactional
    public void updatePortfolio(BankToken bankToken) {
        try {
            // BankToken의 id 를 통해 PENDING 상태인 tokenhistory 내역 가져오기
            TokenHistory pendingTokenHistory = tokenHistoryService
                    .findByBankTokenAndStatus(bankToken, BankTokenStatus.PENDING)
                    .orElseThrow(() -> exceptionDbService.getException("BANK_TOKEN_002"));

            // 기존 포토폴리오 삭제
            portfolioService.clearPortfolios(bankToken);

            portfolioService.createPortfolios(bankToken, pendingTokenHistory);

        } catch (Exception e) {
            throw exceptionDbService.getException("PORTFOLIO_003");
        }

    }



    // 토큰 발행 상태 업데이트(반려)
    @Transactional
    @Override
    public void updateTokenHistoryStatus(RejectBankTokenRequest request, Long tokenHistoryId, BankTokenStatus status) {
        try {
            // 토큰 발행 내역 조회
            TokenHistory tokenHistory = tokenHistoryRepository.findById(tokenHistoryId)
                    .orElseThrow(() -> exceptionDbService.getException("PORTFOLIO_002"));

            // 거절 사유 추가
            tokenHistory.addRejectReason(request.getRejectReason());

            // 상태 업데이트
            tokenHistory.updateStatus(status);
            tokenHistoryRepository.save(tokenHistory);

        } catch (Exception e) {
            throw exceptionDbService.getException("PORTFOLIO_004");
        }
    }


    // 토큰 발행 상태 업데이트(승인)
    @Transactional
    @Override
    public void updateTokenHistoryStatus(Long tokenHistoryId, BankTokenStatus status) {
        try {
            TokenHistory tokenHistory = tokenHistoryRepository.findById(tokenHistoryId)
                    .orElseThrow(() -> exceptionDbService.getException("PORTFOLIO_002"));

            tokenHistory.updateStatus(status);
            tokenHistoryRepository.save(tokenHistory);
        } catch (Exception e) {
            throw exceptionDbService.getException("PORTFOLIO_004");
        }
    }

    @Override
    public TokenHistory findById(Long tokenHistoryId) {
        return tokenHistoryRepository.findById(tokenHistoryId)
                .orElseThrow(() -> exceptionDbService.getException("PORTFOLIO_002"));
    }
}
