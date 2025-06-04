package dev.crepe.domain.core.pay.service.impl;


import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.store.model.StoreType;
import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.pay.service.PayService;
import dev.crepe.domain.core.product.model.BankProductType;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.util.coin.regulation.model.entity.BankToken;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import dev.crepe.domain.core.util.history.pay.model.PayType;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.pay.repostiory.PayHistoryRepository;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final PayHistoryRepository payHistoryRepository;
    private final ExceptionDbService exceptionDbService;
    private final SubscribeHistoryRepository subscribeHistoryRepository;
    private final SubscribeRepository subscribeRepository;

    @Override
    @Transactional
    public void payForOrder(Order order) {
        log.info("결제 내역 생성");
        // 1. 유저와 가맹점의 계좌 정보 조회
        Account userAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getUser().getEmail(), order.getCurrency())
                .orElseThrow(()->exceptionDbService.getException("ACCOUNT_001"));

        Account storeAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getStore().getEmail(), order.getCurrency())
                .orElseThrow(()->exceptionDbService.getException("ACCOUNT_002"));

        accountService.validateAccountNotHold(userAccount);
        accountService.validateAccountNotHold(storeAccount);

        // 2. 결제할 총 금액 계산 (원화 가격 / 코인 환율 = 코인 수량), 소수점 최대 8자리까지 반올림
        BigDecimal totalAmount = BigDecimal.valueOf(order.getTotalPrice())
                .divide(order.getExchangeRate(), 8, RoundingMode.HALF_UP);

        // 3. 유저 잔액 부족 여부 확인
        if (userAccount.getBalance().compareTo(totalAmount) < 0) {
           throw exceptionDbService.getException("ACCOUNT_006");
        }

        // 4. 유저 계좌에서 결제 금액 차감
        userAccount.deductBalance(totalAmount);

        // 5. 결제 내역(PayHistory) 생성 - 아직 승인되지 않은 상태
        PayHistory payHistory = PayHistory.builder()
                .totalAmount(totalAmount)
                .status(PayType.PENDING)
                .order(order)
                .build();

        // 6. 유저 측 거래 내역 생성 - 이미 차감된 상태이므로 ACCEPTED
        TransactionHistory userHistory = TransactionHistory.builder()
                .account(userAccount)
                .amount(totalAmount.negate())
                .afterBalance(userAccount.getBalance())
                .status(TransactionStatus.ACCEPTED)
                .type(TransactionType.PAY)
                .payHistory(payHistory)
                .build();

        // 7. 가맹점 측 거래 내역 생성 - 실제 입금은 스케쥴러로 처리되므로 PENDING
        TransactionHistory storeHistory = TransactionHistory.builder()
                .account(storeAccount)
                .amount(totalAmount)
                .afterBalance(storeAccount.getBalance())
                .status(TransactionStatus.PENDING)
                .type(TransactionType.PAY)
                .payHistory(payHistory)
                .build();

        payHistory.addTransactionHistory(userHistory);
        payHistory.addTransactionHistory(storeHistory);

        // 8. 결제 및 거래 내역 저장
        payHistoryRepository.save(payHistory);
        transactionHistoryRepository.save(userHistory);
        transactionHistoryRepository.save(storeHistory);

    }

    @Transactional
    public void payWithVoucher(Order order, Long subscribeId) {
        log.info("상품권 결제 시작");

        // 1. 현재 주문 요청한 유저 정보 조회
        Actor user = order.getUser();
        Actor store = order.getStore();

        // 2. 결제할 총 금액 계산
        BigDecimal totalAmount = BigDecimal.valueOf(order.getTotalPrice())
                .multiply(order.getExchangeRate())
                .setScale(2, RoundingMode.HALF_UP);

        // 3. 상품권 유효 검사
        Subscribe subscribe = findValidVoucherForPayment(user, order.getStore().getStoreType(), totalAmount, subscribeId);

        order.setVoucher(subscribe);

        // 4. 가맹점 은행 토큰 계좌 정보 조회
        BankToken bankToken = subscribe.getProduct().getBankToken();
        Account storeAccount = accountRepository.findByActorAndBankToken(store, bankToken)
                .orElseThrow(() -> exceptionDbService.getException("ACCOUNT_001"));

        accountService.validateAccountNotHold(storeAccount);


        // 5. 결제 금액 출금
        subscribe.withdraw(totalAmount);

        // 6. 결제 내역(PayHistory) 생성 - 아직 승인되지 않은 상태
        PayHistory payHistory = PayHistory.builder()
                .totalAmount(totalAmount)
                .status(PayType.PENDING)
                .order(order)
                .build();

        // 7. 유저측 결제 내역(SubscribeHistory) 생성
        SubscribeHistory history = SubscribeHistory.builder()
                .subscribe(subscribe)
                .order(order)
                .amount(totalAmount)
                .eventType(SubscribeHistoryType.PAYMENT)
                .afterBalance(subscribe.getBalance())
                .build();

        // 8. 가맹점 측 거래 내역 생성 - 실제 입금은 스케쥴러로 처리되므로 PENDING
        TransactionHistory storeHistory = TransactionHistory.builder()
                .account(storeAccount)
                .amount(totalAmount)
                .afterBalance(storeAccount.getBalance())
                .status(TransactionStatus.PENDING)
                .type(TransactionType.PAY)
                .payHistory(payHistory)
                .build();

        payHistory.addTransactionHistory(storeHistory);

        // 결제 및 거래 내역 저장
        payHistoryRepository.save(payHistory);
        subscribeHistoryRepository.save(history);
        transactionHistoryRepository.save(storeHistory);

    }

    // 유효한 상품권인지 검증
    private Subscribe findValidVoucherForPayment(Actor user, StoreType storeType,
                                                 BigDecimal amount, Long subscribeId) {
        return subscribeRepository.findById(subscribeId)
                .filter(sub -> sub.getUser().equals(user))
                .filter(sub -> sub.getStatus() == SubscribeStatus.ACTIVE)
                .filter(sub -> sub.getProduct().getType() == BankProductType.VOUCHER)
                .filter(sub -> sub.getProduct().getStoreType() == storeType)
                .filter(sub -> sub.getBalance().compareTo(amount) >= 0)
                .orElseThrow(() -> exceptionDbService.getException("PAY_003"));
    }


    @Override
    @Transactional
    public void cancelForOrder(Order order) {
        log.info("주문 취소 내역 생성");
        // 1. 유저 및 스토어 계정 조회
        Account userAccount = accountRepository.findByActor_EmailAndCoin_Currency(
                        order.getUser().getEmail(), order.getCurrency())
                .orElseThrow(()->exceptionDbService.getException("ACCOUNT_001"));

        Account storeAccount = accountRepository.findByActor_EmailAndCoin_Currency(
                        order.getStore().getEmail(), order.getCurrency())
                .orElseThrow(()->exceptionDbService.getException("ACCOUNT_001"));

        // 2. 기존 결제 정보 조회
        PayHistory payHistory = payHistoryRepository.findByOrder(order)
                .orElseThrow(()-> exceptionDbService.getException("PAY_HISTORY_001"));

        // 3. 유저 계정에 결제했던 금액 다시 추가
        userAccount.addAmount(payHistory.getTotalAmount());

        // 4. 결제 상태를 'CANCELED'로 변경 후 저장
        payHistory.cancel();
        payHistoryRepository.save(payHistory);

        // 5. 관련된 거래내역 조회
        List<TransactionHistory> historyList = transactionHistoryRepository.findAllByPayHistory_Order(order);

        // 6. 유저, 가맹점의 거래 타입을 'FAILED'로 변경
        for (TransactionHistory history : historyList) {
                history.cancelTransactionType();
            transactionHistoryRepository.save(history);
        }
    }



    @Transactional
    public void refundForOrder(Long payId, Long id) {
        log.info("환불 내역 생성");
        // 1. 결제 내역 조회
        PayHistory payHistory = payHistoryRepository.findById(payId)
                .orElseThrow(()-> exceptionDbService.getException("PAY_HISTORY_001"));

        // 2. 이미 환불된 상태인지 확인
        if (payHistory.getStatus() == PayType.REFUND) {
            throw exceptionDbService.getException("PAY_001");
        }

        // 3. PayHistory에 연결된 거래 내역들 가져오기
        List<TransactionHistory> txList = payHistory.getTransactionHistories();

        TransactionHistory userTx = null;
        TransactionHistory storeTx = null;

        for (TransactionHistory tx : txList) {
            if (tx.getType() != TransactionType.PAY) continue;

            Long actorId = tx.getAccount().getActor().getId();
            if (actorId==id) {
                userTx = tx;
            } else {
                storeTx = tx;
            }
        }

        // 4. 가맹점 정산 상태 확인
        if (storeTx.getStatus() == TransactionStatus.ACCEPTED) {
            throw exceptionDbService.getException("PAY_002");
        }

        // 5. 상태 변경 처리
        storeTx.refundTransactionStatus();     // 가맹점 거래 상태 변경
        payHistory.refund();                   // 결제 상태 환불로 변경
        userTx.getAccount().addAmount(userTx.getAmount().negate()); // 잔액 복원

        // 6. 유저 환불 기록 생성
        TransactionHistory refundTx = TransactionHistory.builder()
                .account(userTx.getAccount())
                .type(TransactionType.REFUND)
                .status(TransactionStatus.ACCEPTED)
                .amount(userTx.getAmount())
                .afterBalance(userTx.getAccount().getBalance().negate())
                .payHistory(payHistory)
                .build();

        transactionHistoryRepository.save(refundTx);
    }


}
