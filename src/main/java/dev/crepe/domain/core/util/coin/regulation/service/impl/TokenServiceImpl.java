package dev.crepe.domain.core.util.coin.regulation.service.impl;


import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.admin.dto.response.GetAllBankTokenResponse;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.util.coin.regulation.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.service.TokenService;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.service.PortfolioHistoryService;
import dev.crepe.domain.core.util.history.token.service.TokenHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenHistoryService tokenHistoryService;
    private final AccountService accountService;
    private final PortfolioHistoryService portfolioHistoryService;
    private final BankTokenRepository bankTokenRepository;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveBankTokenRequest(Long tokenHistoryId) {

        // TokenHistory 조회
        TokenHistory tokenHistory = tokenHistoryService.findById(tokenHistoryId);
        BankToken bankToken = tokenHistory.getBankToken();

        // 계좌 활성화
        accountService.activeBankTokenAccount(bankToken, tokenHistory);
        // 포토폴리오 변경 내역 추가
        portfolioHistoryService.updatePortfolio(bankToken);
        // 토큰 발행 내역 추가
        portfolioHistoryService.updateTokenHistoryStatus(tokenHistoryId, BankTokenStatus.APPROVED);

        // 토큰 발행 승인
        bankToken.approve();
        bankToken.changeTotalSupply(tokenHistory.getTotalSupplyAmount());
        bankTokenRepository.save(bankToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectBankTokenRequest(RejectBankTokenRequest request, Long tokenHistoryId) {

        // TokenHistory 조회
        TokenHistory tokenHistory = tokenHistoryService.findById(tokenHistoryId);
        // 토큰 발행 상태 변경
        portfolioHistoryService.updateTokenHistoryStatus(request, tokenHistory.getId(), BankTokenStatus.REJECTED);
    }

}