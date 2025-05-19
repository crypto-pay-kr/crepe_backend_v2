package dev.crepe.domain.core.pay.service.impl;


import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.exception.NotEnoughAmountException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.pay.exception.AlreadyRefundException;
import dev.crepe.domain.core.pay.exception.StoreAlreadySettledException;
import dev.crepe.domain.core.util.history.pay.execption.PayHistoryNotFoundException;
import dev.crepe.domain.core.pay.service.PayService;
import dev.crepe.domain.core.util.history.pay.model.PayType;
import dev.crepe.domain.core.util.history.pay.model.entity.PayHistory;
import dev.crepe.domain.core.util.history.pay.repostiory.PayHistoryRepository;
import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import dev.crepe.domain.core.util.history.business.model.TransactionType;
import dev.crepe.domain.core.util.history.business.model.entity.TransactionHistory;
import dev.crepe.domain.core.util.history.business.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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


    @Override
    @Transactional
    public void cancelForOrder(Order order) {

        // 1. 유저 및 스토어 계정 조회
        Account userAccount = accountRepository.findByActor_EmailAndCoin_Currency(
                        order.getUser().getEmail(), order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getUser().getEmail()));

        Account storeAccount = accountRepository.findByActor_EmailAndCoin_Currency(
                        order.getStore().getEmail(), order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getStore().getEmail()));

        // 2. 기존 결제 정보 조회
        PayHistory payHistory = payHistoryRepository.findByOrder(order)
                .orElseThrow(PayHistoryNotFoundException::new);

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
    public void refundForOrder(Long payId, String email) {
        // 1. 결제 내역 조회
        PayHistory payHistory = payHistoryRepository.findById(payId)
                .orElseThrow(PayHistoryNotFoundException::new);

        // 2. 이미 환불된 상태인지 확인
        if (payHistory.getStatus() == PayType.REFUND) {
            throw new AlreadyRefundException();
        }

        // 3. PayHistory에 연결된 거래 내역들 가져오기
        List<TransactionHistory> txList = payHistory.getTransactionHistories();

        TransactionHistory userTx = null;
        TransactionHistory storeTx = null;

        for (TransactionHistory tx : txList) {
            if (tx.getType() != TransactionType.PAY) continue;

            String actorEmail = tx.getAccount().getActor().getEmail();
            if (actorEmail.equals(email)) {
                userTx = tx;
            } else {
                storeTx = tx;
            }
        }

        // 4. 가맹점 정산 상태 확인
        if (storeTx.getStatus() == TransactionStatus.ACCEPTED) {
            throw new StoreAlreadySettledException();
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
