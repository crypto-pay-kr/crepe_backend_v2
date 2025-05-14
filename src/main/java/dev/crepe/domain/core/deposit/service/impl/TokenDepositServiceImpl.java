package dev.crepe.domain.core.deposit.service.impl;

import dev.crepe.domain.core.account.exception.NotEnoughAmountException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.deposit.service.TokenDepositService;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.product.repository.ProductRepository;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TokenDepositServiceImpl implements TokenDepositService {

    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final SubscribeRepository subscribeRepository;

    @Transactional
    public String depositToProduct(Long actorId, Long productId, BigDecimal amount) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 1. 토큰 계좌 잔액 확인 및 차감
        Account account = accountRepository.findByActorIdAndBankToken(actorId, product.getBankToken())
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new NotEnoughAmountException("잔액이 부족합니다");
        }

        account.reduceAmount(amount);


        return "예치 완료";


    }

    private void handleVoucher(Account account, Product product, BigDecimal amount) {
        // 1. 가입 정보 조회
        Subscribe subscribe = subscribeRepository
                .findByUserIdAndProductId(account.getActor().getId(), product.getId())
                .orElseThrow(() -> new RuntimeException("가입된 상품권이 없습니다"));

        BigDecimal unitPrice = product.getBudget(); // 상품권 1장당 가격
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("상품권 단가가 올바르지 않습니다");
        }

        // 2. 단가로 나누어떨어지는지 체크
        if (amount.remainder(unitPrice).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("상품권 구입 금액은 단가의 배수여야 합니다");
        }

        // 3. 구입
        subscribe.deposit(subscribe.getBalance().add(amount));

        subscribeRepository.save(subscribe);
    }


}

