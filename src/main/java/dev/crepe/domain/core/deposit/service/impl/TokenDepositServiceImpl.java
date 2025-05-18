package dev.crepe.domain.core.deposit.service.impl;

import dev.crepe.domain.core.account.exception.NotEnoughAmountException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.deposit.service.TokenDepositService;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenDepositServiceImpl implements TokenDepositService {

    private final AccountRepository accountRepository;
    private final SubscribeRepository subscribeRepository;
    private final SubscribeHistoryRepository subscribeHistoryRepository;

    @Transactional
    public String depositToProduct(String userEmail, Long subscribeId, BigDecimal amount) {
        // 1. 상품이 있는지 확인
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(() -> new RuntimeException("가입 정보가 없습니다"));

        Product product = subscribe.getProduct();

        // 2. 토큰 계좌 확인
        Account account = accountRepository.findByActor_EmailAndBankTokenId(userEmail, product.getBankToken().getId())
                .orElseThrow(() -> new RuntimeException("사용자의 은행 토큰 계좌가 없습니다."));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new NotEnoughAmountException("잔액이 부족합니다");
        }

        switch (product.getType()) {
            case VOUCHER -> handleDepositVoucher(account, subscribe, amount); // 상품권
            case SAVING -> handleDepositSaving(account, subscribe, amount); // 예금
            case INSTALLMENT -> handleDepositInstallment(account, subscribe, amount); // 적금
        }
        return "예치 완료";
    }


    // 상품권 예치
    private void handleDepositVoucher(Account account, Subscribe subscribe, BigDecimal amount) {
        Product product = subscribe.getProduct();
        // 1. 상품 가입 상태 확인
        validateActiveSubscribe(subscribe);

        // 2. 월 최대 한도 체크 (월 최대 한도 초과시 예외 처리)
        validateDepositLimit(subscribe, product, amount);

        // 3. 할인율 적용
        BigDecimal discount = BigDecimal.valueOf(product.getBaseInterestRate());
        BigDecimal discountRatio = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100)));
        BigDecimal discountedAmount = amount.multiply(discountRatio);

        // 4. 계좌 잔액 차감
        account.reduceAmount(discountedAmount);

        // 5. 상품에 amount 추가
        subscribe.deposit(amount);
        subscribeRepository.save(subscribe);

        // 6. subscribe 거래내역에 예치 완료 내역 찍기
        saveDepositHistory(subscribe, amount);
    }

    // 예금 예치
    private void handleDepositSaving(Account account, Subscribe subscribe, BigDecimal amount) {
        Product product = subscribe.getProduct();
        // 1. 상품 가입 상태 확인
        validateActiveSubscribe(subscribe);

        // 2. 이미 예치한 경우 예외 처리
        if (subscribe.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("예금은 한 번만 예치할 수 있습니다.");
        }

        // 5. 예치 최대 한도 체크 (예치 최대 한도 초과시 예외 처리)
        BigDecimal maxLimit = product.getMaxMonthlyPayment(); // 예금 예치 최대 한도

        if (maxLimit != null && amount.compareTo(maxLimit) > 0) {
            throw new IllegalArgumentException("예금 최대 예치 한도를 초과했습니다");
        }

        // 6. 해당 bankToken 계좌에서 amount 차감
        account.reduceAmount(amount);

        // 7. 상품에 amount 추가
        subscribe.deposit(amount);
        subscribeRepository.save(subscribe);

        // 8. subscribe 거래내역에 예치 완료 내역 찍기
        saveDepositHistory(subscribe, amount);

    }


    // 적금 예치
    private void handleDepositInstallment(Account account, Subscribe subscribe, BigDecimal amount) {
        Product product = subscribe.getProduct();
        // 1. 상품 가입 상태 확인
        validateActiveSubscribe(subscribe);

        // 2. 월 최대 한도 체크
        validateDepositLimit(subscribe, product, amount);

        // 3. 해당 bankToken 계좌에서 amount 차감
        // Account bankTokenAccount = getBankTokenAccount(account.getActor().getEmail(), product.getBankToken().getId());
        account.reduceAmount(amount);

        // 4. 상품에 amount 추가
        subscribe.deposit(amount);
        subscribeRepository.save(subscribe);

        // 5. subscribe 거래내역에 예치 완료 내역 찍기
        saveDepositHistory(subscribe, amount);
    }



    // subscribe 거래내역에 예치 완료 내역 저장
    private void saveDepositHistory(Subscribe subscribe, BigDecimal amount) {
        SubscribeHistory history = SubscribeHistory.builder()
                .subscribe(subscribe)
                .eventType(SubscribeHistoryType.DEPOSIT)
                .amount(amount)
                .build();
        subscribeHistoryRepository.save(history);
    }


    // 월 최대 한도 체크 (월 최대 한도 초과시 예외 처리)
    void validateDepositLimit(Subscribe subscribe, Product product, BigDecimal amount) {
        BigDecimal maxLimit = product.getMaxMonthlyPayment(); // 월 최대 한도 금액

        if (maxLimit != null) {
            // 현재 달 기준 시작/끝 계산
            LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);

            // 이 달의 누적 입금액 조회
            BigDecimal depositedThisMonth = subscribeHistoryRepository.sumMonthlyDeposit(
                    subscribe, SubscribeHistoryType.DEPOSIT, startOfMonth, endOfMonth
            );

            // 총액 계산
            BigDecimal totalAfterDeposit = depositedThisMonth.add(amount);
            if (totalAfterDeposit.compareTo(maxLimit) > 0) {
                throw new IllegalArgumentException("월 최대 예치 한도를 초과했습니다");
            }
        }
    }

    // 상품 가입 상태 검증
    private void validateActiveSubscribe(Subscribe subscribe) {
        if (!subscribe.isActive()) {
            throw new IllegalStateException("예치 가능한 상태가 아닙니다. (현재 상태: " + subscribe.getStatus() + ")");
        }
    }
}

