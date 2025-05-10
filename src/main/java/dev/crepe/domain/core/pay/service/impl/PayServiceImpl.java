package dev.crepe.domain.core.pay.service.impl;


import dev.crepe.domain.channel.market.order.model.entity.Order;
import dev.crepe.domain.core.account.exception.AccountNotFoundException;
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

        Account userAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getUser().getEmail(),order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getUser().getEmail()));

        Account storeAccount = accountRepository.findByActor_EmailAndCoin_Currency(order.getStore().getEmail(),order.getCurrency())
                .orElseThrow(() -> new AccountNotFoundException(order.getStore().getEmail()));

        BigDecimal totalAmount = BigDecimal.valueOf(order.getTotalPrice())
                .divide(order.getExchangeRate(), 8, RoundingMode.HALF_UP);

        userAccount.reduceAmount(totalAmount);

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
}
