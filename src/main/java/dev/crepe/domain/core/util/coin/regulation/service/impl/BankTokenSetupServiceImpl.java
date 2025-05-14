package dev.crepe.domain.core.util.coin.regulation.service.impl;

import dev.crepe.domain.bank.model.dto.request.CreateBankTokenRequest;
import dev.crepe.domain.bank.model.entity.Bank;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.core.util.coin.global.repository.PortfolioRepository;
import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import dev.crepe.domain.core.util.coin.non_regulation.repository.CoinRepository;
import dev.crepe.domain.core.util.history.token.model.BankTokenStatus;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.coin.regulation.model.entity.Portfolio;
import dev.crepe.domain.core.util.coin.regulation.model.entity.TokenPrice;
import dev.crepe.domain.core.util.coin.regulation.repository.BankTokenRepository;
import dev.crepe.domain.core.util.coin.regulation.repository.TokenPriceRepository;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenSetupService;
import dev.crepe.domain.core.util.history.token.model.entity.TokenHistory;
import dev.crepe.domain.core.util.history.token.repository.TokenHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankTokenSetupServiceImpl implements BankTokenSetupService {

    private final TokenPriceRepository tokenPriceRepository;
    private final BankTokenRepository bankTokenRepository;
    private final PortfolioRepository portfolioRepository;
    private final CoinRepository coinRepository;
    private final BankRepository bankRepository;
    private final TokenHistoryRepository tokenHistoryRepository;

    @Override
    @Transactional
    public ResponseEntity<Void> requestTokenGenerate(CreateBankTokenRequest request, String bankEmail) {

        Bank bank = bankRepository.findByEmail(bankEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 등록된 은행이 존재하지 않습니다."));


        if(bankTokenRepository.existsByBank_Id(bank.getId())) {
            throw new IllegalStateException("이미 발행 요청된 토큰이 존재합니다.");
        }

        // 토큰 시가총액 계산
        BigDecimal total = calculateTotalPrice(request);

        BankToken bankToken = BankToken.builder()
                .bank(bank)
                .name(request.getBankName())
                .currency(request.getTokenCurrency())
                .totalSupply(total)
                .status(BankTokenStatus.PENDING)
                .build();
        bankTokenRepository.save(bankToken);

        TokenPrice tokenPrice = TokenPrice.builder()
                .bankToken(bankToken)
                .price(total)
                .build();
        tokenPriceRepository.save(tokenPrice);

        // Portfolio 생성 및 저장
        request.getPortfolioCoins().forEach(coinInfo -> {
            Coin coin = coinRepository.findByCurrency(coinInfo.getCurrency());

            Portfolio portfolio = Portfolio.builder()
                    .bankToken(bankToken)
                    .coin(coin)
                    .amount(coinInfo.getAmount())
                    .initialPrice(coinInfo.getCurrentPrice())
                    .build();
            portfolioRepository.save(portfolio);
        });

        // TokenHistory 생성 및 저장
        TokenHistory tokenHistory = TokenHistory.builder()
                .bankToken(bankToken)
                .status(BankTokenStatus.PENDING)
                .amount(total)
                .description(request.getDescription())
                .build();
        tokenHistoryRepository.save(tokenHistory);


        // 로그 출력
        log.info("Calculated total price: {}", total);

        return ResponseEntity.ok().build();
    }

    private BigDecimal calculateTotalPrice(CreateBankTokenRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (CreateBankTokenRequest.CoinInfo coin : request.getPortfolioCoins()) {
            if (coin.getAmount() != null && coin.getCurrentPrice() != null) {
                total = total.add(coin.getAmount().multiply(coin.getCurrentPrice()));
            }
        }
        return total;
    }
}