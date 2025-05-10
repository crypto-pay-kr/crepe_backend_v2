package dev.crepe.domain.core.pay.service.impl;


import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.pay.exception.PayHistoryNotFoundException;
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

        // 1. 계좌 조회
        Account userAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getUser().getEmail(),order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getUser().getEmail()));

        Account storeAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getStore().getEmail(),order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getStore().getEmail()));


        //2. 총 가격(KRW)/코인 한개당 가격 (KRW)= 총 코인 갯수
        BigDecimal totalAmount = BigDecimal.valueOf(order.getTotalPrice())
                .divide(order.getExchangeRate(), 8, RoundingMode.HALF_UP);

        // 3. 유저계좌에서 금액 차감
        userAccount.reduceAmount(totalAmount);

        //4. 거래및 결제 내역 기록
        PayHistory payHistory = PayHistory.builder()
                .totalAmount(totalAmount)
                .status(PayType.PENDING)
                .order(order)
                .build();

        TransactionHistory userHistory = TransactionHistory.builder()
                .account(userAccount)
                .amount(totalAmount)
                .status(TransactionStatus.ACCEPTED)
                .type(TransactionType.PAY)
                .payHistory(payHistory)
                .build();


        TransactionHistory storeHistory = TransactionHistory.builder()
                .account(storeAccount)
                .amount(totalAmount)
                .status(TransactionStatus.PENDING)
                .type(TransactionType.PAY)
                .payHistory(payHistory)
                .build();

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

        Account storeAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getStore().getEmail(),order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getStore().getEmail()));

        // 2. 기존 결제 정보 조회
        PayHistory payHistory = payHistoryRepository.findByOrder(order)
                .orElseThrow(PayHistoryNotFoundException::new);

        userAccount.addAmount(payHistory.getTotalAmount());

        payHistory.cancel();
        payHistoryRepository.save(payHistory);

        // 3. 거래 내역 조회 및 상태 변경
        List<TransactionHistory> historyList = transactionHistoryRepository.findAllByPayHistory_Order(order);

        for (TransactionHistory history : historyList) {
            Long accountId = history.getAccount().getId();

            if (accountId.equals(userAccount.getId()) && history.getType() == TransactionType.PAY) {
                history.cancelTransactionStatus();
            }

            if (accountId.equals(storeAccount.getId())) {
                history.cancelTransactionStatus();
            }

            transactionHistoryRepository.save(history);
        }

    }
}
