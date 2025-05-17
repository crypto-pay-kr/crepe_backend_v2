package dev.crepe.domain.core.util.history.token.service.impl;

import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.dto.request.ReCreateBankTokenRequest;
import dev.crepe.domain.core.util.coin.global.repository.PortfolioRepository;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.history.token.model.TokenRequestType;
import dev.crepe.domain.core.util.history.token.model.entity.PortfolioHistoryDetail;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.PortfolioHistoryDetailRepository;
import dev.crepe.domain.core.util.history.token.repository.TokenHistoryRepository;
import dev.crepe.domain.core.util.history.token.service.PortfolioHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioHistoryServiceImpl implements PortfolioHistoryService {

    private final PortfolioRepository portfolioRepository;
    private final TokenHistoryRepository tokenHistoryRepository;
    private final PortfolioHistoryDetailRepository portfolioHistoryDetailRepository;
    private final CoinRepository coinRepository;


    // 토큰 포토폴리오 신규 내역 저장
    @Override
    @Transactional
    public void addTokenPortfolioHistory(CreateBankTokenRequest request, BankToken bankToken, BigDecimal totalSupplyAmount) {
        // TokenHistory 생성
        TokenHistory tokenHistory = TokenHistory.builder()
                .bankToken(bankToken)
                .totalSupplyAmount(totalSupplyAmount)
                .status(BankTokenStatus.PENDING)
                .requestType(TokenRequestType.NEW)
                .build();
        tokenHistoryRepository.save(tokenHistory);

        // PortfolioHistoryDetail 추가
        request.getPortfolioCoins().forEach(portfolio -> {
            PortfolioHistoryDetail detail = PortfolioHistoryDetail.builder()
                    .tokenHistory(tokenHistory)
                    .coinName(portfolio.getCoinName())
                    .coinCurrency(portfolio.getCurrency())
                    .updateAmount(portfolio.getAmount())
                    .updatePrice(portfolio.getCurrentPrice())
                    .build();
            portfolioHistoryDetailRepository.save(detail);
        });
    }


    // 토큰 재발행 요청 이력 업데이트
    @Override
    @Transactional
    public void updateTokenHistory(ReCreateBankTokenRequest request, BankToken bankToken, BigDecimal totalSupplyAmount) {

        // TokenHistory 생성
        TokenHistory tokenHistory = TokenHistory.builder()
                .bankToken(bankToken)
                .totalSupplyAmount(totalSupplyAmount)
                .changeReason(request.getChangeReason())
                .status(BankTokenStatus.PENDING)
                .requestType(TokenRequestType.UPDATE)
                .build();
        tokenHistoryRepository.save(tokenHistory);

        // PortfolioHistoryDetail 추가
        request.getPortfolioCoins().forEach(portfolio -> {
            // 기존 Portfolio 데이터 조회
            var existingPortfolio = portfolioRepository.findByBankTokenAndCoinCurrency(bankToken, portfolio.getCurrency());

            // prevAmount와 prevPrice 설정
            BigDecimal prevAmount = existingPortfolio.map(p -> p.getAmount()).orElse(BigDecimal.ZERO);
            BigDecimal prevPrice = existingPortfolio.map(p -> p.getInitialPrice()).orElse(BigDecimal.ZERO);

            PortfolioHistoryDetail detail = PortfolioHistoryDetail.builder()
                    .tokenHistory(tokenHistory)
                    .coinName(portfolio.getCoinName())
                    .coinCurrency(portfolio.getCurrency())
                    .prevAmount(prevAmount) // 이전 값 저장
                    .prevPrice(prevPrice)   // 이전 값 저장
                    .updateAmount(portfolio.getAmount()) // 수정된 값
                    .updatePrice(portfolio.getCurrentPrice()) // 수정된 값
                    .build();
            portfolioHistoryDetailRepository.save(detail);
        });
    }


    // 토큰 발행 승인 이후 포토폴리오 업데이트
    @Override
    @Transactional
    public void updatePortfolio(BankToken bankToken) {

        // BankToken의 id 를 통해 PENDING 상태인 tokenhistory 내역 가져오기
        TokenHistory pendingTokenHistory = tokenHistoryRepository.findByBankTokenAndStatus(bankToken, BankTokenStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("PENDING 상태의 TokenHistory를 찾을 수 없습니다."));

        // 기존 포토폴리오 삭제
        bankToken.getPortfolios().clear();
        portfolioRepository.deleteAllByBankToken(bankToken);

        pendingTokenHistory.getPortfolioDetails().forEach(portfolio -> {
            Coin coin = coinRepository.findByCurrency(portfolio.getCoinCurrency());
            if (coin == null) {
                throw new IllegalArgumentException("해당 통화에 대한 Coin 정보를 찾을 수 없습니다: " + portfolio.getCoinCurrency());
            }
            Portfolio newPortfolio = Portfolio.builder()
                    .bankToken(bankToken)
                    .coin(coin)
                    .amount(portfolio.getUpdateAmount())
                    .initialPrice(portfolio.getUpdatePrice())
                    .build();
            portfolioRepository.save(newPortfolio);
        });
    }



    // 토큰 발행 상태 업데이트(반려)
    @Transactional
    @Override
    public void updateTokenHistoryStatus(RejectBankTokenRequest request, Long tokenHistoryId, BankTokenStatus status) {

        // 토큰 발행 내역 조회
        TokenHistory tokenHistory = tokenHistoryRepository.findById(tokenHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 TokenHistory를 찾을 수 없습니다: " + tokenHistoryId));

        tokenHistory.addRejectReason(request.getRejectReason());

        System.out.println("거절 사유: " + request.getRejectReason());
        // 상태 업데이트
        tokenHistory.updateStatus(status);
        tokenHistoryRepository.save(tokenHistory);
    }

    // 토큰 발행 상태 업데이트(승인)
    @Transactional
    @Override
    public void updateTokenHistoryStatus(Long tokenHistoryId, BankTokenStatus status) {

        // 토큰 발행 내역 조회
        TokenHistory tokenHistory = tokenHistoryRepository.findById(tokenHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 TokenHistory를 찾을 수 없습니다: " + tokenHistoryId));

        // 상태 업데이트
        tokenHistory.updateStatus(status);
        tokenHistoryRepository.save(tokenHistory);
    }
}
