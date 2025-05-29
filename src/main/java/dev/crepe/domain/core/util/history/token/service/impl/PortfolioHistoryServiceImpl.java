package dev.crepe.domain.core.util.history.token.service.impl;

import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.coin.regulation.exception.PendingBankTokenExistsException;
import dev.crepe.domain.core.util.coin.regulation.exception.PortfolioUpdateFailedException;
import dev.crepe.domain.core.util.coin.regulation.exception.TokenHistoryNotFoundException;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.repository.PortfolioRepository;
import dev.crepe.domain.core.util.coin.regulation.service.PortfolioService;
import dev.crepe.domain.core.util.history.token.model.TokenRequestType;
import dev.crepe.domain.core.util.history.token.model.entity.PortfolioHistoryDetail;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.PortfolioHistoryDetailRepository;
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
            log.error("포트폴리오 히스토리 추가 중 오류 발생: {}", e.getMessage(), e);
            throw new PortfolioUpdateFailedException("포트폴리오 히스토리 추가 중 오류 발생: " + e.getMessage());
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
            log.error("토큰 재발행 요청 이력 업데이트 중 오류 발생: {}", e.getMessage(), e);
            exceptionDbService.throwException("PORTFOLIO_02");
        }
    }


    // 토큰 발행 승인 이후 포토폴리오 업데이트
    @Override
    @Transactional
    public void updatePortfolio(BankToken bankToken) {
        try {
            // BankToken의 id 를 통해 PENDING 상태인 tokenhistory 내역 가져오기
            TokenHistory pendingTokenHistory = tokenHistoryService.findByBankTokenAndStatus(bankToken, BankTokenStatus.PENDING)
                    .orElseThrow(() -> new PendingBankTokenExistsException(bankToken.getName()));

            // 기존 포토폴리오 삭제
            portfolioService.clearPortfolios(bankToken);

            portfolioService.createPortfolios(bankToken, pendingTokenHistory);

        } catch (Exception e) {
            log.error("포트폴리오 업데이트 중 오류 발생: {}", e.getMessage(), e);
            exceptionDbService.throwException("PORTFOLIO_03");
        }

    }



    // 토큰 발행 상태 업데이트(반려)
    @Transactional
    @Override
    public void updateTokenHistoryStatus(RejectBankTokenRequest request, Long tokenHistoryId, BankTokenStatus status) {
        try {
            // 토큰 발행 내역 조회
            TokenHistory tokenHistory = tokenHistoryRepository.findById(tokenHistoryId)
                    .orElseThrow(() -> new TokenHistoryNotFoundException(tokenHistoryId));

            // 거절 사유 추가
            tokenHistory.addRejectReason(request.getRejectReason());

            // 상태 업데이트
            tokenHistory.updateStatus(status);
            tokenHistoryRepository.save(tokenHistory);

        } catch (Exception e) {
            log.error("토큰 히스토리 상태 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new PortfolioUpdateFailedException("토큰 히스토리 상태 업데이트 중 오류가 발생했습니다.");
        }
    }


    // 토큰 발행 상태 업데이트(승인)
    @Transactional
    @Override
    public void updateTokenHistoryStatus(Long tokenHistoryId, BankTokenStatus status) {
        try {
            TokenHistory tokenHistory = tokenHistoryRepository.findById(tokenHistoryId)
                    .orElseThrow(() -> new TokenHistoryNotFoundException(tokenHistoryId));

            tokenHistory.updateStatus(status);
            tokenHistoryRepository.save(tokenHistory);
        } catch (Exception e) {
            log.error("토큰 히스토리 상태 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new PortfolioUpdateFailedException("토큰 히스토리 상태 업데이트 중 오류가 발생했습니다.");
        }
    }

    @Override
    public TokenHistory findById(Long tokenHistoryId) {
        return tokenHistoryRepository.findById(tokenHistoryId)
                .orElseThrow(() -> new TokenHistoryNotFoundException(tokenHistoryId));
    }
}
