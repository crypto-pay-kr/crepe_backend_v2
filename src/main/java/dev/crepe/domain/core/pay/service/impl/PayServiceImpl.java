package dev.crepe.domain.core.pay.service.impl;


import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.exception.NotEnoughAmountException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.pay.service.PayService;
import dev.crepe.domain.core.util.history.pay.model.PayType;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.pay.repostiory.PayHistoryRepository;
import dev.crepe.domain.core.util.history.transfer.model.TransactionStatus;
import dev.crepe.domain.core.util.history.transfer.model.TransactionType;
import dev.crepe.domain.core.util.history.transfer.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.transfer.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

   private final AccountRepository accountRepository;
   private final TransactionHistoryRepository transactionHistoryRepository;
   private final PayHistoryRepository payHistoryRepository;

    @Override
    @Transactional
    public void payForOrder(Order order) {

        // 1. 유저와 가맹점의 계좌 정보 조회
        Account userAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getUser().getEmail(), order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getUser().getEmail()));

        Account storeAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getStore().getEmail(), order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getStore().getEmail()));

        // 2. 결제할 총 금액 계산 (원화 가격 / 코인 환율 = 코인 수량), 소수점 최대 8자리까지 반올림
        BigDecimal totalAmount = BigDecimal.valueOf(order.getTotalPrice())
                .divide(order.getExchangeRate(), 8, RoundingMode.HALF_UP);

        // 3. 유저 잔액 부족 여부 확인
        if (userAccount.getBalance().compareTo(totalAmount) < 0) {
            throw new NotEnoughAmountException(order.getCurrency());
        }

        // 4. 유저 계좌에서 결제 금액 차감
        userAccount.reduceAmount(totalAmount);

        // 5. 결제 내역(PayHistory) 생성 - 아직 승인되지 않은 상태
        PayHistory payHistory = PayHistory.builder()
                .totalAmount(totalAmount)
                .status(PayType.PENDING)
                .order(order)
                .build();

        // 6. 유저 측 거래 내역 생성 - 이미 차감된 상태이므로 ACCEPTED
        TransactionHistory userHistory = TransactionHistory.builder()
                .account(userAccount)
                .amount(totalAmount)
                .status(TransactionStatus.ACCEPTED)
                .type(TransactionType.PAY)
                .payHistory(payHistory)
                .build();

        // 7. 가맹점 측 거래 내역 생성 - 실제 입금은 추후 승인 시 처리되므로 PENDING
        TransactionHistory storeHistory = TransactionHistory.builder()
                .account(storeAccount)
                .amount(totalAmount)
                .status(TransactionStatus.PENDING)
                .type(TransactionType.PAY)
                .payHistory(payHistory)
                .build();

        // 8. 결제 및 거래 내역 저장
        payHistoryRepository.save(payHistory);
        transactionHistoryRepository.save(userHistory);
        transactionHistoryRepository.save(storeHistory);
    }
}
